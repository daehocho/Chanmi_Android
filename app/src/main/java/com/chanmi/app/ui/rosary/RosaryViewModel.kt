package com.chanmi.app.ui.rosary

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chanmi.app.data.model.DecadeStep
import com.chanmi.app.data.model.MysteryType
import com.chanmi.app.data.model.RosaryPhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RosaryViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val PREFERRED_HAND_KEY = stringPreferencesKey("preferredHand")
        private val HAS_SEEN_SWIPE_GUIDE_KEY = booleanPreferencesKey("hasSeenSwipeGuide")
    }

    private val _currentPhase = MutableStateFlow<RosaryPhase>(RosaryPhase.MysterySelection)
    val currentPhase: StateFlow<RosaryPhase> = _currentPhase.asStateFlow()

    private val _selectedMystery = MutableStateFlow(MysteryType.recommendedForToday())
    val selectedMystery: StateFlow<MysteryType> = _selectedMystery.asStateFlow()

    private val _numberOfDecades = MutableStateFlow(5)
    val numberOfDecades: StateFlow<Int> = _numberOfDecades.asStateFlow()

    private val _isPraying = MutableStateFlow(false)
    val isPraying: StateFlow<Boolean> = _isPraying.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    val preferredHand: StateFlow<String> = dataStore.data
        .map { it[PREFERRED_HAND_KEY] ?: "right" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "right")

    val hasSeenSwipeGuide: StateFlow<Boolean> = dataStore.data
        .map { it[HAS_SEEN_SWIPE_GUIDE_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var timerJob: Job? = null
    private var lastAdvanceTime = 0L
    private val advanceDebounceMs = 300L

    // ===== Computed Properties =====

    val formattedTime: String
        get() {
            val s = _elapsedSeconds.value
            return String.format("%02d:%02d", s / 60, s % 60)
        }

    val progress: Double
        get() {
            val total = totalSteps
            if (total <= 0) return 0.0
            return currentStepIndex.toDouble() / total.toDouble()
        }

    val progressPercent: String
        get() = "${(progress * 100).toInt()}%"

    val currentPrayerText: String
        get() = when (val phase = _currentPhase.value) {
            is RosaryPhase.MysterySelection -> ""
            is RosaryPhase.SignOfCross -> "성부와 성자와 성령의 이름으로. 아멘."
            is RosaryPhase.ApostlesCreed -> "전능하신 천주 성부, 천지의 창조주를 저는 믿나이다. 그 외아들 우리 주 예수 그리스도님, " +
                    "성령으로 인하여 동정 마리아에게서 나시고, 본시오 빌라도 통치 아래에서 고난을 받으시고, " +
                    "십자가에 못박혀 돌아가시고, 묻히셨으며, 저승에 가시어 사흗날에 죽은 이들 가운데서 부활하시고, " +
                    "하늘에 올라 전능하신 천주 성부 오른편에 앉으시며, 그리로부터 산 이와 죽은 이를 심판하러 오시리라 믿나이다. " +
                    "성령을 믿으며, 거룩하고 보편된 교회와 모든 성인의 통공을 믿으며, 죄의 용서와 육신의 부활을 믿으며, " +
                    "영원한 삶을 믿나이다. 아멘."
            is RosaryPhase.OpeningOurFather -> ourFatherText
            is RosaryPhase.OpeningHailMary -> hailMaryText
            is RosaryPhase.OpeningGlory -> gloryBeText
            is RosaryPhase.Decade -> when (phase.step) {
                is DecadeStep.Meditation -> "제${phase.number}단: ${_selectedMystery.value.meditations[phase.number - 1]}"
                is DecadeStep.OurFather -> ourFatherText
                is DecadeStep.HailMary -> hailMaryText
                is DecadeStep.Glory -> gloryBeText
                is DecadeStep.Fatima -> fatimaText
            }
            is RosaryPhase.SalveRegina -> salveReginaText
            is RosaryPhase.ClosingPrayer -> "성부와 성자와 성령의 이름으로. 아멘."
            is RosaryPhase.Completed -> ""
        }

    val currentPhaseTitle: String
        get() = when (val phase = _currentPhase.value) {
            is RosaryPhase.MysterySelection -> ""
            is RosaryPhase.SignOfCross -> "성호경"
            is RosaryPhase.ApostlesCreed -> "사도신경"
            is RosaryPhase.OpeningOurFather -> "주님의 기도"
            is RosaryPhase.OpeningHailMary -> "성모송 (${phase.count}/3)"
            is RosaryPhase.OpeningGlory -> "영광송"
            is RosaryPhase.Decade -> {
                val stepName = when (phase.step) {
                    is DecadeStep.Meditation -> "묵상"
                    is DecadeStep.OurFather -> "주님의 기도"
                    is DecadeStep.HailMary -> "성모송 (${phase.step.count}/10)"
                    is DecadeStep.Glory -> "영광송"
                    is DecadeStep.Fatima -> "파티마의 기도"
                }
                "제${phase.number}단 - $stepName"
            }
            is RosaryPhase.SalveRegina -> "성모찬송"
            is RosaryPhase.ClosingPrayer -> "마침 기도"
            is RosaryPhase.Completed -> "완료"
        }

    val currentMeditationTopic: String?
        get() {
            val phase = _currentPhase.value
            return if (phase is RosaryPhase.Decade) {
                _selectedMystery.value.meditations[phase.number - 1]
            } else null
        }

    val currentDecade: Int?
        get() {
            val phase = _currentPhase.value
            return if (phase is RosaryPhase.Decade) phase.number else null
        }

    private val totalSteps: Int
        get() = 9 + 14 * _numberOfDecades.value

    private val currentStepIndex: Int
        get() = when (val phase = _currentPhase.value) {
            is RosaryPhase.MysterySelection -> 0
            is RosaryPhase.SignOfCross -> 1
            is RosaryPhase.ApostlesCreed -> 2
            is RosaryPhase.OpeningOurFather -> 3
            is RosaryPhase.OpeningHailMary -> 3 + phase.count
            is RosaryPhase.OpeningGlory -> 7
            is RosaryPhase.Decade -> {
                val base = 8 + (phase.number - 1) * 14
                when (phase.step) {
                    is DecadeStep.Meditation -> base
                    is DecadeStep.OurFather -> base + 1
                    is DecadeStep.HailMary -> base + 1 + phase.step.count
                    is DecadeStep.Glory -> base + 12
                    is DecadeStep.Fatima -> base + 13
                }
            }
            is RosaryPhase.SalveRegina -> 8 + _numberOfDecades.value * 14
            is RosaryPhase.ClosingPrayer -> 8 + _numberOfDecades.value * 14 + 1
            is RosaryPhase.Completed -> totalSteps
        }

    // ===== Actions =====

    fun selectMystery(mystery: MysteryType) {
        _selectedMystery.value = mystery
    }

    fun setNumberOfDecades(count: Int) {
        _numberOfDecades.value = count
    }

    fun startPraying() {
        _isPraying.value = true
        _currentPhase.value = RosaryPhase.SignOfCross
        _elapsedSeconds.value = 0
        startTimer()
    }

    fun stopPraying() {
        timerJob?.cancel()
        timerJob = null
        _isPraying.value = false
    }

    fun debouncedAdvance() {
        val now = System.currentTimeMillis()
        if (now - lastAdvanceTime < advanceDebounceMs) return
        lastAdvanceTime = now
        advance()
    }

    fun advance() {
        _currentPhase.value = when (val phase = _currentPhase.value) {
            is RosaryPhase.MysterySelection -> RosaryPhase.SignOfCross
            is RosaryPhase.SignOfCross -> RosaryPhase.ApostlesCreed
            is RosaryPhase.ApostlesCreed -> RosaryPhase.OpeningOurFather
            is RosaryPhase.OpeningOurFather -> RosaryPhase.OpeningHailMary(1)
            is RosaryPhase.OpeningHailMary -> {
                if (phase.count < 3) RosaryPhase.OpeningHailMary(phase.count + 1)
                else RosaryPhase.OpeningGlory
            }
            is RosaryPhase.OpeningGlory -> RosaryPhase.Decade(1, DecadeStep.Meditation)
            is RosaryPhase.Decade -> {
                when (phase.step) {
                    is DecadeStep.Meditation -> RosaryPhase.Decade(phase.number, DecadeStep.OurFather)
                    is DecadeStep.OurFather -> RosaryPhase.Decade(phase.number, DecadeStep.HailMary(1))
                    is DecadeStep.HailMary -> {
                        if (phase.step.count < 10) RosaryPhase.Decade(phase.number, DecadeStep.HailMary(phase.step.count + 1))
                        else RosaryPhase.Decade(phase.number, DecadeStep.Glory)
                    }
                    is DecadeStep.Glory -> RosaryPhase.Decade(phase.number, DecadeStep.Fatima)
                    is DecadeStep.Fatima -> {
                        if (phase.number < _numberOfDecades.value) RosaryPhase.Decade(phase.number + 1, DecadeStep.Meditation)
                        else RosaryPhase.SalveRegina
                    }
                }
            }
            is RosaryPhase.SalveRegina -> RosaryPhase.ClosingPrayer
            is RosaryPhase.ClosingPrayer -> RosaryPhase.Completed
            is RosaryPhase.Completed -> RosaryPhase.Completed
        }
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        _isPraying.value = false
        _elapsedSeconds.value = 0
        _currentPhase.value = RosaryPhase.MysterySelection
        _selectedMystery.value = MysteryType.recommendedForToday()
        _numberOfDecades.value = 5
    }

    fun setPreferredHand(hand: String) {
        viewModelScope.launch {
            dataStore.edit { it[PREFERRED_HAND_KEY] = hand }
        }
    }

    fun markSwipeGuideSeen() {
        viewModelScope.launch {
            dataStore.edit { it[HAS_SEEN_SWIPE_GUIDE_KEY] = true }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    // ===== Prayer Texts =====

    private val ourFatherText = "하늘에 계신 우리 아버지, 아버지의 이름이 거룩히 빛나시며, " +
            "아버지의 나라가 오시며, 아버지의 뜻이 하늘에서와 같이 " +
            "땅에서도 이루어지소서. 오늘 저희에게 일용할 양식을 주시고, " +
            "저희에게 잘못한 이를 저희가 용서하오니 저희 죄를 용서하시고, " +
            "저희를 유혹에 빠지지 않게 하시고, 악에서 구하소서. 아멘."

    private val hailMaryText = "은총이 가득하신 마리아님, 기뻐하소서. 주님께서 함께 계시니 " +
            "여인 중에 복되시며, 태중의 아들 예수님 또한 복되시나이다. " +
            "천주의 성모 마리아님, 이제와 저희 죽을 때에 저희 죄인을 위하여 " +
            "빌어주소서. 아멘."

    private val gloryBeText = "영광이 성부와 성자와 성령께, 처음과 같이 이제와 항상 " +
            "영원히. 아멘."

    private val fatimaText = "오 예수님, 저희 죄를 용서하시고, 저희를 지옥 불에서 구하시며, " +
            "모든 영혼, 특히 가장 불쌍한 영혼을 천국으로 이끌어 주소서."

    private val salveReginaText = "구원을 비는 노래를 부르오니, 자비로우신 어머니, 우리의 생명이시요, " +
            "기쁨이시며, 희망이신 성모님, 비오니 이 세상 귀양살이하는 이브의 자손들이 " +
            "눈물을 흘리며 성모님께 부르짖나이다. 우리를 위하여 빌어주시는 이여, " +
            "그 자비로운 눈을 돌리시어 이 귀양살이를 마친 뒤에 복되신 태중의 열매 " +
            "예수를 저희에게 보여 주소서. 자비롭고 자애롭고 감미로우신 동정 마리아님."
}

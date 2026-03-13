package com.chanmi.app.data.model

sealed class DecadeStep {
    data object Meditation : DecadeStep()
    data object OurFather : DecadeStep()
    data class HailMary(val count: Int) : DecadeStep() // 1~10
    data object Glory : DecadeStep()
    data object Fatima : DecadeStep()
}

sealed class RosaryPhase {
    data object MysterySelection : RosaryPhase()
    data object SignOfCross : RosaryPhase()
    data object ApostlesCreed : RosaryPhase()
    data object OpeningOurFather : RosaryPhase()
    data class OpeningHailMary(val count: Int) : RosaryPhase() // 1~3
    data object OpeningGlory : RosaryPhase()
    data class Decade(val number: Int, val step: DecadeStep) : RosaryPhase() // 1~5
    data object SalveRegina : RosaryPhase()
    data object ClosingPrayer : RosaryPhase()
    data object Completed : RosaryPhase()
}

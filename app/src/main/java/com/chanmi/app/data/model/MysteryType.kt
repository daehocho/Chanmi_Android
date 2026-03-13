package com.chanmi.app.data.model

enum class MysteryType(val key: String) {
    JOYFUL("joyful"),
    SORROWFUL("sorrowful"),
    GLORIOUS("glorious"),
    LUMINOUS("luminous");

    val displayName: String
        get() = when (this) {
            JOYFUL -> "환희의 신비"
            SORROWFUL -> "고통의 신비"
            GLORIOUS -> "영광의 신비"
            LUMINOUS -> "빛의 신비"
        }

    val englishName: String
        get() = when (this) {
            JOYFUL -> "Joyful Mysteries"
            SORROWFUL -> "Sorrowful Mysteries"
            GLORIOUS -> "Glorious Mysteries"
            LUMINOUS -> "Luminous Mysteries"
        }

    val recommendedDays: String
        get() = when (this) {
            JOYFUL -> "월요일, 토요일"
            SORROWFUL -> "화요일, 금요일"
            GLORIOUS -> "수요일, 일요일"
            LUMINOUS -> "목요일"
        }

    val description: String
        get() = when (this) {
            JOYFUL -> "예수 그리스도의 탄생과 어린 시절의 기쁜 사건들을 묵상합니다"
            SORROWFUL -> "예수 그리스도의 수난과 죽음의 고통스러운 사건들을 묵상합니다"
            GLORIOUS -> "예수 그리스도의 부활과 승천, 성령 강림의 영광스러운 사건들을 묵상합니다"
            LUMINOUS -> "예수 그리스도의 공생활 중 빛나는 사건들을 묵상합니다"
        }

    val meditations: List<String>
        get() = when (this) {
            JOYFUL -> listOf(
                "마리아께서 예수님을 잉태하심을 묵상합시다",
                "마리아께서 엘리사벳을 찾아보심을 묵상합시다",
                "마리아께서 예수님을 낳으심을 묵상합시다",
                "마리아께서 예수님을 성전에 바치심을 묵상합시다",
                "마리아께서 잃으셨던 예수님을 성전에서 찾으심을 묵상합시다"
            )
            SORROWFUL -> listOf(
                "예수님께서 우리를 위하여 피땀 흘리심을 묵상합시다",
                "예수님께서 우리를 위하여 매맞으심을 묵상합시다",
                "예수님께서 우리를 위하여 가시관 쓰심을 묵상합시다",
                "예수님께서 우리를 위하여 십자가 지심을 묵상합시다",
                "예수님께서 우리를 위하여 십자가에 못박혀 돌아가심을 묵상합시다"
            )
            GLORIOUS -> listOf(
                "예수님께서 부활하심을 묵상합시다",
                "예수님께서 승천하심을 묵상합시다",
                "예수님께서 성령을 보내심을 묵상합시다",
                "마리아께서 하늘에 올림을 받으심을 묵상합시다",
                "마리아께서 하늘과 땅의 모후로 뽑히심을 묵상합시다"
            )
            LUMINOUS -> listOf(
                "예수님께서 요르단 강에서 세례 받으심을 묵상합시다",
                "예수님께서 카나 혼인잔치에서 첫 기적을 행하심을 묵상합시다",
                "예수님께서 하느님 나라를 선포하심을 묵상합시다",
                "예수님께서 거룩하게 변모하심을 묵상합시다",
                "예수님께서 성체성사를 세우심을 묵상합시다"
            )
        }

    companion object {
        fun recommendedForToday(): MysteryType {
            return when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.SUNDAY -> GLORIOUS
                java.util.Calendar.MONDAY -> JOYFUL
                java.util.Calendar.TUESDAY -> SORROWFUL
                java.util.Calendar.WEDNESDAY -> GLORIOUS
                java.util.Calendar.THURSDAY -> LUMINOUS
                java.util.Calendar.FRIDAY -> SORROWFUL
                java.util.Calendar.SATURDAY -> JOYFUL
                else -> JOYFUL
            }
        }

        fun fromKey(key: String): MysteryType {
            return entries.first { it.key == key }
        }
    }
}

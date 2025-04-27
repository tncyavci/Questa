package com.example.questa.data.model

/**
 * Anket istatistiklerini temsil eden veri sınıfı
 * Bu sınıf, bir anketin toplam oy sayısı, demografik dağılımı,
 * ve zaman içindeki katılım oranları gibi istatistiksel bilgilerini içerir.
 */
data class PollStats(
    val pollId: String = "",
    val totalVotes: Int = 0,
    val optionStats: List<OptionStat> = listOf(),
    val demographicData: DemographicData = DemographicData(),
    val participationOverTime: Map<String, Int> = mapOf() // Tarih -> Oy sayısı
)

/**
 * Bir anket seçeneğinin istatistiklerini içeren veri sınıfı
 */
data class OptionStat(
    val optionId: String = "",
    val optionText: String = "",
    val votes: Int = 0,
    val percentage: Float = 0f
)

/**
 * Ankete katılanların demografik bilgilerini içeren veri sınıfı
 */
data class DemographicData(
    val ageGroups: Map<String, Int> = mapOf(), // Yaş grubu -> Katılımcı sayısı
    val genders: Map<String, Int> = mapOf(),   // Cinsiyet -> Katılımcı sayısı
    val locations: Map<String, Int> = mapOf()  // Konum -> Katılımcı sayısı
) 
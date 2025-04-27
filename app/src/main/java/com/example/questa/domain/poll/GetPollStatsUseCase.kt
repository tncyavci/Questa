package com.example.questa.domain.poll

import com.example.questa.data.model.PollStats
import com.example.questa.data.repository.PollRepository
import kotlinx.coroutines.flow.Flow

/**
 * Belirli bir anketin katılım istatistiklerini getiren kullanım durumu.
 * Bu istatistikler toplam oy sayısı, demografik bilgiler ve zamana göre
 * katılım oranlarını içerir.
 */
class GetPollStatsUseCase(private val pollRepository: PollRepository) {
    operator fun invoke(pollId: String): Flow<PollStats?> {
        return pollRepository.getPollStats(pollId)
    }
} 
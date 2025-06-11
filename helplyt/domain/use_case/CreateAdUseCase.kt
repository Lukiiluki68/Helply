package com.example.app.domain.use_case

import Ad
import com.example.app.data.repository.AdRepository

class CreateAdUseCase(
    private val repository: AdRepository
) {
    suspend operator fun invoke(ad: Ad) {
        repository.createAd(ad)
    }
}

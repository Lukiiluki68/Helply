package com.example.app.domain.use_case

import com.example.app.data.repository.AdRepository
import com.example.app.model.Ad

class CreateAdUseCase(
    private val repository: AdRepository
) {
    suspend operator fun invoke(ad: Ad) {
        repository.createAd(ad)
    }
}

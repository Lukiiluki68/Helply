import com.example.app.domain.repository.UserRepository
import com.example.helplyt.domain.model.UserProfile

class SaveUserProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.saveUserProfile(profile)
    }
}


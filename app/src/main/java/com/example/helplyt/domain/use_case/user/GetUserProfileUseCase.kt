import com.example.app.domain.repository.UserRepository

class GetUserProfileUseCase(private val repo: UserRepository) {
    suspend operator fun invoke() = repo.getUserProfile()
}

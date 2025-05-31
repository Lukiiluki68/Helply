import com.example.app.domain.repository.UserRepository

class ChangePasswordUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) {
        repository.changePassword(currentPassword, newPassword)
    }
}

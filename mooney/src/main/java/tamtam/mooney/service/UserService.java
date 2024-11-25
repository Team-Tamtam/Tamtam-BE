package tamtam.mooney.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final String testAccountEmail = "test@domain.com";

    @PostConstruct
    public void createTestAccount() {
        if (userRepository.findById(1L).isEmpty()) {
            User testAccount = User.builder()
                    .userId(1L)
                    .email(testAccountEmail)
                    .loginType("GOOGLE")
                    .nickname("화연")
                    .build();
            userRepository.save(testAccount);
        }
    }

    @Transactional(readOnly = true)
    public String getUserNickname() {
        User user = getCurrentUser();
        return user.getNickname();
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() throws CustomException {
        return userRepository.findByEmail(testAccountEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_AUTHENTICATED));
    }
}

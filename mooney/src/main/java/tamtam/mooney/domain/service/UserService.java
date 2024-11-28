package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.domain.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public String getCurrentUserNickname() {
        return getCurrentUser().getNickname();
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() throws CustomException {
        return userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_AUTHENTICATED));
    }
}

package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.UserSchedule;
import tamtam.mooney.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
    List<UserSchedule> findByUserAndStartDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    List<UserSchedule> findByUserAndIsRepeatingTrue(User user);
}
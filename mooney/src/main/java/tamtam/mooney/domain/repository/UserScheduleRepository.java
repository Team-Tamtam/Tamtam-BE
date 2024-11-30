package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.entity.UserSchedule;
import tamtam.mooney.domain.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserScheduleRepository extends JpaRepository<UserSchedule, Long> {
    List<UserSchedule> findByUserAndStartDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}
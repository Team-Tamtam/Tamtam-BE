package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.Schedule;
import tamtam.mooney.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserAndStartDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Schedule> findByUserAndIsRepeatingTrue(User user);
}
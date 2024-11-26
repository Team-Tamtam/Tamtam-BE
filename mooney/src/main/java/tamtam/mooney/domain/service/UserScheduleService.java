package tamtam.mooney.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.UserScheduleDto;
import tamtam.mooney.domain.entity.UserSchedule;

import tamtam.mooney.domain.entity.User;
import tamtam.mooney.domain.repository.UserScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class UserScheduleService {
    private final UserScheduleRepository userScheduleRepository;
    private final UserService userService;

    public List<UserScheduleDto> getSchedulesForTomorrow() {
        User user = userService.getCurrentUser();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        List<UserSchedule> tomorrowSchedules = userScheduleRepository.findByUserAndStartDateTimeBetween(user, startOfTomorrow, endOfTomorrow);

        return tomorrowSchedules.stream()
                .map(schedule -> new UserScheduleDto(
                        schedule.getScheduleId(),
                        schedule.getTitle(),
                        schedule.getStartDateTime(),
                        schedule.getEndDateTime(),
                        schedule.getLocation())
                )
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void initSchedules() {
        User user = userService.getCurrentUser();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        if (userScheduleRepository.findByUserAndStartDateTimeBetween(user, startOfTomorrow, startOfTomorrow.plusDays(1)).isEmpty()) {
            createAndSaveDefaultSchedules(user, startOfTomorrow);
        }
    }

    // This method will create and save the default schedules
    private void createAndSaveDefaultSchedules(User user, LocalDateTime startOfTomorrow) {
        List<UserSchedule> defaultSchedules = new ArrayList<>();

        defaultSchedules.add(UserSchedule.builder()
                .title("채플 시작")
                .startDateTime(startOfTomorrow.plusHours(10))
                .endDateTime(startOfTomorrow.plusHours(11))
                .location("이화여자대학교 대강당")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());

        defaultSchedules.add(UserSchedule.builder()
                .title("3시반 미술 전시회")
                .startDateTime(startOfTomorrow.plusHours(14))
                .endDateTime(startOfTomorrow.plusHours(16))
                .location("그라운드시소 서촌")
                .user(user)
                .build());

        defaultSchedules.add(UserSchedule.builder()
                .title("바른학원")
                .startDateTime(startOfTomorrow.plusHours(18))
                .endDateTime(startOfTomorrow.plusHours(19))
                .location("바른학원")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());

        // Save all default schedules
        userScheduleRepository.saveAll(defaultSchedules);
    }
}
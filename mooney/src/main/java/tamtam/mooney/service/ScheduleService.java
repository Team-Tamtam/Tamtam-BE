package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.ScheduleDto;
import tamtam.mooney.entity.Schedule;

import tamtam.mooney.entity.User;
import tamtam.mooney.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserService userService;

    @Transactional
    public List<ScheduleDto> getOrInsertSchedulesForTomorrow() {
        User user = userService.getCurrentUser();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        // 내일 일정 데이터 가져오기
        List<Schedule> tomorrowSchedules = scheduleRepository.findByUserAndStartDateTimeBetween(user, startOfTomorrow, endOfTomorrow);

        // 내일 일정이 없으면 기본 일정 생성 및 저장
        if (tomorrowSchedules.isEmpty()) {
            tomorrowSchedules = createAndSaveDefaultSchedules(user, startOfTomorrow);
        }

        // 내일 일정 DTO로 변환하여 반환
        return tomorrowSchedules.stream()
                .map(schedule -> new ScheduleDto(
                        schedule.getScheduleId(),
                        schedule.getTitle(),
                        schedule.getStartDateTime(),
                        schedule.getEndDateTime(),
                        schedule.getLocation())
                )
                .collect(Collectors.toList());
    }

    private List<Schedule> createAndSaveDefaultSchedules(User user, LocalDateTime startOfTomorrow) {
        List<Schedule> defaultSchedules = new ArrayList<>();

        defaultSchedules.add(Schedule.builder()
                .title("채플 시작")
                .startDateTime(startOfTomorrow.plusHours(10))
                .endDateTime(startOfTomorrow.plusHours(11))
                .location("이화여자대학교 대강당")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());

        defaultSchedules.add(Schedule.builder()
                .title("3시반 미술 전시회")
                .startDateTime(startOfTomorrow.plusHours(14))
                .endDateTime(startOfTomorrow.plusHours(16))
                .location("그라운드시소 서촌")
                .user(user)
                .build());

        defaultSchedules.add(Schedule.builder()
                .title("바른학원")
                .startDateTime(startOfTomorrow.plusHours(18))
                .endDateTime(startOfTomorrow.plusHours(19))
                .location("바른학원")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());
        return scheduleRepository.saveAll(defaultSchedules);
    }
}
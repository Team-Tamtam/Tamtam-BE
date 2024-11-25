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
        List<Schedule> tomorrowSchedules = scheduleRepository.findByStartDateTimeBetween(startOfTomorrow, endOfTomorrow);

        // 데이터가 없는 경우 기본 일정 추가
        if (tomorrowSchedules.isEmpty()) {
            List<Schedule> defaultSchedules = createDefaultSchedulesForTomorrow(user);
            scheduleRepository.saveAll(defaultSchedules);
            tomorrowSchedules = defaultSchedules;
        }

        // 전체 일정 반환
        List<Schedule> allSchedules = scheduleRepository.findAll();

        return allSchedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private List<Schedule> createDefaultSchedulesForTomorrow(User user) {
        List<Schedule> defaultSchedules = new ArrayList<>();
        LocalDateTime tomorrowDate = LocalDate.now().plusDays(1).atStartOfDay();

        defaultSchedules.add(Schedule.builder()
                .title("채플 시작")
                .startDateTime(tomorrowDate.plusHours(10))
                .endDateTime(tomorrowDate.plusHours(11))
                .location("이화여자대학교 대강당")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());

        defaultSchedules.add(Schedule.builder()
                .title("3시반 미술 전시회")
                .startDateTime(tomorrowDate.plusHours(14))
                .endDateTime(tomorrowDate.plusHours(16))
                .location("그라운드시소 서촌")
                .user(user)
                .build());

        defaultSchedules.add(Schedule.builder()
                .title("바른학원")
                .startDateTime(tomorrowDate.plusHours(18))
                .endDateTime(tomorrowDate.plusHours(19))
                .location("바른학원")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());
        return defaultSchedules;
    }

    private ScheduleDto convertToDto(Schedule schedule) {
        return ScheduleDto.builder()
                .scheduleId(schedule.getScheduleId())
                .title(schedule.getTitle())
                .startDateTime(schedule.getStartDateTime())
                .endDateTime(schedule.getEndDateTime())
                .location(schedule.getLocation())
                .build();
    }
}

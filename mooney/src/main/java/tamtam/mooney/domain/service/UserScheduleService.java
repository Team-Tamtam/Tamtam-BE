package tamtam.mooney.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.common.UserScheduleDto;
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
}
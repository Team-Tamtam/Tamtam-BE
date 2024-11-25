package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.DailyBudgetRequestDto;
import tamtam.mooney.dto.DailyBudgetResponseDto;
import tamtam.mooney.entity.Schedule;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.DailyBudgetRepository;
import tamtam.mooney.repository.ScheduleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DailyBudgetService {
    private final UserService userService;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final ScheduleRepository scheduleRepository;

    public DailyBudgetResponseDto getTomorrowBudgetAndSchedules(DailyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // 반복 일정 조회 (반복 설정된 일정 가져오기)
        List<Schedule> repeatedSchedules = scheduleRepository.findByUserAndIsRepeatingTrue(user);

        // dto에서 받은 일정 조회
        List<Schedule> tomorrowSchedules = scheduleRepository.findAllById(requestDto.getScheduleIds());

        List<DailyBudgetResponseDto.RepeatedScheduleDto> repeatedScheduleDTOs = repeatedSchedules.stream()
                .map(schedule -> DailyBudgetResponseDto.RepeatedScheduleDto.builder()
                        .scheduleId(schedule.getScheduleId())
                        .title(schedule.getTitle())
                        .categoryName(schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN")
                        .predictedAmount(schedule.getPredictedAmount() != null ? schedule.getPredictedAmount() : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        List<DailyBudgetResponseDto.TomorrowScheduleDto> tomorrowScheduleDTOs = tomorrowSchedules.stream()
                .map(schedule -> DailyBudgetResponseDto.TomorrowScheduleDto.builder()
                        .scheduleId(schedule.getScheduleId())
                        .title(schedule.getTitle())
                        .categoryName(schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN")
                        .predictedAmount(schedule.getPredictedAmount() != null ? schedule.getPredictedAmount() : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        // TODO: GPT를 호출하여 예산 계산 (임시값, 실제 구현 필요)
        BigDecimal budgetAmount = BigDecimal.valueOf(20000);

        return DailyBudgetResponseDto.builder()
                .repeatedSchedules(repeatedScheduleDTOs)
                .tomorrowSchedules(tomorrowScheduleDTOs)
                .budgetAmount(budgetAmount)
                .build();
    }
}

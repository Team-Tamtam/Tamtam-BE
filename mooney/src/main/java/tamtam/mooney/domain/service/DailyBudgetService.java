package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.request.DailyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.DailyBudgetResponseDto;
import tamtam.mooney.domain.entity.UserSchedule;
import tamtam.mooney.domain.entity.User;
import tamtam.mooney.domain.repository.DailyBudgetRepository;
import tamtam.mooney.domain.repository.UserScheduleRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DailyBudgetService {
    private final UserService userService;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final UserScheduleRepository userScheduleRepository;

    public DailyBudgetResponseDto getTomorrowBudgetAndSchedules(DailyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // 반복 일정 조회 (반복 설정된 일정 가져오기)
        List<UserSchedule> repeatedSchedules = userScheduleRepository.findByUserAndIsRepeatingTrue(user);

        // dto에서 받은 일정 조회
        List<UserSchedule> tomorrowSchedules = userScheduleRepository.findAllById(requestDto.scheduleIds());

        // predictedAmount가 0인 일정은 제외
        List<DailyBudgetResponseDto.RepeatedScheduleDto> repeatedScheduleDTOs = repeatedSchedules.stream()
                .filter(schedule -> schedule.getPredictedAmount() != null && schedule.getPredictedAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(schedule -> DailyBudgetResponseDto.RepeatedScheduleDto.builder()
                        .scheduleId(schedule.getScheduleId())
                        .title(schedule.getTitle())
                        .categoryName(schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN")
                        .predictedAmount(schedule.getPredictedAmount() != null ? schedule.getPredictedAmount() : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        // 내일 일정에서 중복된 항목 제거 (반복 일정과 동일한 일정은 제외)
        List<DailyBudgetResponseDto.TomorrowScheduleDto> tomorrowScheduleDTOs = tomorrowSchedules.stream()
                .filter(schedule -> schedule.getPredictedAmount() != null && schedule.getPredictedAmount().compareTo(BigDecimal.ZERO) > 0)
                .filter(schedule -> repeatedSchedules.stream().noneMatch(repeatedSchedule -> repeatedSchedule.getScheduleId().equals(schedule.getScheduleId())))
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

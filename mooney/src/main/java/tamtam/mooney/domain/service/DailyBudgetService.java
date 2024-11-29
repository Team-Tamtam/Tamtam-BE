package tamtam.mooney.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.request.DailyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.DailyBudgetResponseDto;
import tamtam.mooney.domain.entity.MonthlyBudget;
import tamtam.mooney.domain.entity.UserSchedule;
import tamtam.mooney.domain.entity.User;
import tamtam.mooney.domain.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.repository.UserScheduleRepository;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.global.openai.AIPromptService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBudgetService {
    private final UserService userService;
    private final UserScheduleRepository userScheduleRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final AIPromptService aiPromptService;

    @Transactional(readOnly = true)
    public DailyBudgetResponseDto getTomorrowBudgetAndSchedules(DailyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();
        // 반복 일정 조회 (조건 체크 생략)
        List<UserSchedule> repeatedSchedules = userScheduleRepository.findByUserAndIsRepeatingTrue(user);
        // DTO에서 받은 내일 일정 조회
        List<UserSchedule> tomorrowSchedules = userScheduleRepository.findAllById(requestDto.scheduleIds());

        // 반복 일정 데이터 구성
        List<Map<String, Object>> recurringExpenses = repeatedSchedules.stream()
                .filter(schedule -> schedule.getPredictedAmount() != null && schedule.getPredictedAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(schedule -> {
                    Map<String, Object> expense = new HashMap<>();
                    expense.put("scheduleId", schedule.getScheduleId());
                    expense.put("title", schedule.getTitle());
                    expense.put("category", schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN");
                    expense.put("predictedAmount", schedule.getPredictedAmount());
                    return expense;
                })
                .toList();

        // 내일 일정 데이터 구성
        List<Map<String, Object>> scheduledExpenses = tomorrowSchedules.stream()
                .filter(schedule -> schedule.getPredictedAmount() != null && schedule.getPredictedAmount().compareTo(BigDecimal.ZERO) > 0)
                .filter(schedule -> repeatedSchedules.stream()
                        .noneMatch(repeatedSchedule -> repeatedSchedule.getScheduleId().equals(schedule.getScheduleId())))
                .map(schedule -> {
                    Map<String, Object> expense = new HashMap<>();
                    expense.put("scheduleId", schedule.getScheduleId());
                    expense.put("title", schedule.getTitle());
                    expense.put("category", schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN");
                    expense.put("predictedAmount", schedule.getPredictedAmount());
                    return expense;
                })
                .toList();

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        MonthlyBudget thisMonthBudget = monthlyBudgetRepository.findByUserAndPeriod(user, formattedDate)
                .orElseThrow(()-> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        BigDecimal totalBudget = thisMonthBudget.getFinalAmount();

        double weightForCategory = 0.5;

        String aiResponse = aiPromptService.buildDailyBudgetMessage(
                recurringExpenses,
                scheduledExpenses,
                totalBudget,
                weightForCategory
        );
        log.info("AI Response:\n" + aiResponse);

        try {
            return mergeSchedulesWithJsonData(repeatedSchedules, tomorrowSchedules, aiResponse);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public DailyBudgetResponseDto mergeSchedulesWithJsonData(
            List<UserSchedule> repeatedSchedules,
            List<UserSchedule> tomorrowSchedules,
            String jsonResponse
    ) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // JSON에서 recurring_expense와 scheduled_expenses 추출
        List<Map<String, Object>> recurringExpenseData = objectMapper.convertValue(
                rootNode.get("recurring_expense"),
                new TypeReference<List<Map<String, Object>>>() {}
        );
        List<Map<String, Object>> scheduledExpenseData = objectMapper.convertValue(
                rootNode.get("scheduled_expenses"),
                new TypeReference<List<Map<String, Object>>>() {}
        );

        // 반복 일정 데이터 매핑
        List<DailyBudgetResponseDto.RepeatedScheduleDto> repeatedScheduleDtos = repeatedSchedules.stream()
                .map(schedule -> {
                    // JSON 데이터에서 일치하는 카테고리를 찾아 병합
                    Map<String, Object> jsonExpense = recurringExpenseData.stream()
                            .filter(expense -> schedule.getCategoryName().name().equals(expense.get("category")))
                            .findFirst()
                            .orElse(null);

                    // 예산 값을 JSON에서 가져오거나 기존 값을 사용
                    BigDecimal predictedAmount = (jsonExpense != null)
                            ? new BigDecimal(String.valueOf(jsonExpense.get("today_amount")))
                            : schedule.getPredictedAmount();

                    return DailyBudgetResponseDto.RepeatedScheduleDto.builder()
                            .scheduleId(schedule.getScheduleId())
                            .title(schedule.getTitle())
                            .categoryName(schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN")
                            .predictedAmount(predictedAmount)
                            .build();
                })
                .collect(Collectors.toList());

        // 내일 일정 데이터 매핑
        List<DailyBudgetResponseDto.TomorrowScheduleDto> tomorrowScheduleDtos = tomorrowSchedules.stream()
                .filter(schedule -> repeatedSchedules.stream()
                        .noneMatch(repeated -> repeated.getScheduleId().equals(schedule.getScheduleId())))
                .map(schedule -> {
                    // JSON 데이터에서 일치하는 설명을 찾아 병합
                    Map<String, Object> jsonExpense = scheduledExpenseData.stream()
                            .filter(expense -> schedule.getTitle().equals(expense.get("description")))
                            .findFirst()
                            .orElse(null);

                    // 예산 값을 JSON에서 가져오거나 기존 값을 사용
                    BigDecimal predictedAmount = (jsonExpense != null)
                            ? new BigDecimal(String.valueOf(jsonExpense.get("amount")))
                            : schedule.getPredictedAmount();

                    return DailyBudgetResponseDto.TomorrowScheduleDto.builder()
                            .scheduleId(schedule.getScheduleId())
                            .title(schedule.getTitle())
                            .categoryName(schedule.getCategoryName() != null ? schedule.getCategoryName().name() : "UNKNOWN")
                            .predictedAmount(predictedAmount)
                            .build();
                })
                .collect(Collectors.toList());

        // 전체 예산 추출
        BigDecimal budgetAmount = rootNode.has("daily_budget_total")
                ? new BigDecimal(rootNode.get("daily_budget_total").asText())
                : BigDecimal.ZERO;

        return DailyBudgetResponseDto.builder()
                .repeatedSchedules(repeatedScheduleDtos)
                .tomorrowSchedules(tomorrowScheduleDtos)
                .budgetAmount(budgetAmount)
                .build();
    }

}

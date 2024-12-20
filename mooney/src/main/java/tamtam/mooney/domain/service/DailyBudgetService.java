package tamtam.mooney.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.request.DailyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.DailyBudgetResponseDto;
import tamtam.mooney.domain.entity.*;
import tamtam.mooney.domain.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.repository.UserScheduleRepository;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.global.openai.AIPromptService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DailyBudgetService {
    private final UserService userService;
    private final UserScheduleRepository userScheduleRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final AIPromptService aiPromptService;
    private final ExpenseService expenseService;

    public DailyBudgetResponseDto getTomorrowBudgetAndSchedules(DailyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // DTO에서 받은 내일 일정 조회
        List<UserSchedule> tomorrowSchedules = userScheduleRepository.findAllById(requestDto.scheduleIds());

        // 내일 일정 데이터 구성
        List<Map<String, Object>> tomorrowScheduleMapList = tomorrowSchedules.stream()
                .map(schedule -> {
                    Map<String, Object> expense = new HashMap<>();
                    expense.put("schedule_id", schedule.getScheduleId());
                    expense.put("title", schedule.getTitle());
                    return expense;
                })
                .toList();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String period = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        MonthlyBudget monthBudget = monthlyBudgetRepository.findByUserAndPeriod(user, period)
                .orElseThrow(()-> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        BigDecimal totalBudget = monthBudget.getFinalAmount();
        List<Expense> expenses = expenseService.findExpensesForUser(tomorrow.getYear(), tomorrow.getMonthValue(), user);
        BigDecimal totalExpenseAmount = expenseService.calculateTotalExpenseAmount(expenses);

        double weightForCategory = 0.5;
        if (totalBudget.compareTo(totalExpenseAmount) < 0) {
            throw new CustomException(ErrorCode.ERROR);
        }
        try {
            String aiResponse = aiPromptService.buildDailyBudgetMessage(
                    tomorrow,
                    tomorrowScheduleMapList,
                    totalBudget,
                    weightForCategory
            );
            log.info("AI Response:\n" + aiResponse);
            return mergeSchedulesWithJsonData(tomorrowSchedules, aiResponse);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public DailyBudgetResponseDto mergeSchedulesWithJsonData(
            List<UserSchedule> tomorrowSchedules,
            String jsonResponse
    ) throws Exception {
        log.info("tomorrowSchedules: " + tomorrowSchedules);
        log.info("jsonResponse: " + jsonResponse);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // JSON에서 recurring_expenses 추출
        List<Map<String, Object>> recurringExpenseData = Optional.ofNullable(rootNode.get("recurring_expenses"))
                .map(node -> objectMapper.convertValue(node, new TypeReference<List<Map<String, Object>>>() {}))
                .orElse(Collections.emptyList());

        // JSON에서 scheduled_expenses 추출
        List<Map<String, Object>> scheduledExpenseData = Optional.ofNullable(rootNode.get("scheduled_expenses"))
                .map(node -> objectMapper.convertValue(node, new TypeReference<List<Map<String, Object>>>() {}))
                .orElse(Collections.emptyList());

        // 반복 소비 데이터 매핑
        List<DailyBudgetResponseDto.RepeatedScheduleDto> repeatedScheduleDtos = recurringExpenseData.stream()
                .map(expense -> {
                    String category = (String) expense.get("category");
                    BigDecimal perDayAmount = new BigDecimal(String.valueOf(expense.get("per_day_amount")));

                    return new DailyBudgetResponseDto.RepeatedScheduleDto(
                            null,
                            category,
                            category,
                            perDayAmount
                    );
                })
                .collect(Collectors.toList());

        // 내일 일정 데이터 매핑
        List<DailyBudgetResponseDto.TomorrowScheduleDto> tomorrowScheduleDtos = Optional.ofNullable(tomorrowSchedules)
                .orElse(Collections.emptyList()) // 내일 일정이 없을 경우 빈 리스트 처리
                .stream()
                .map(schedule -> {
                    // JSON 데이터에서 일치하는 설명을 찾아 병합
                    Map<String, Object> jsonExpense = scheduledExpenseData.stream()
                            .filter(data -> schedule.getScheduleId().equals(data.get("schedule_id")))
                            .findFirst()
                            .orElse(null);

                    CategoryName categoryName = Optional.ofNullable(jsonExpense)
                            .map(expense -> Arrays.stream(CategoryName.values())
                                    .filter(c -> c.getDescription().equals(expense.get("category")))
                                    .findFirst()
                                    .orElse(CategoryName.EXTRA))
                            .orElse(CategoryName.EXTRA);


                    // Retrieve and check weighted_budget
                    BigDecimal predictedAmount = Optional.ofNullable(jsonExpense)
                            .map(expense -> {
                                Object weightedBudget = expense.get("weighted_budget");
                                // Log the weighted_budget value for debugging
                                log.info("weighted_budget: " + weightedBudget);
                                if (weightedBudget != null && !weightedBudget.toString().isEmpty()) {
                                    try {
                                        return new BigDecimal(weightedBudget.toString());
                                    } catch (NumberFormatException e) {
                                        log.error("Invalid number format for weighted_budget: " + weightedBudget, e);
                                        return schedule.getPredictedAmount(); // fallback
                                    }
                                }
                                return schedule.getPredictedAmount(); // fallback if null or empty
                            })
                            .orElse(schedule.getPredictedAmount());
                    log.info("predictedAmount: " + predictedAmount);


                    schedule.setCategoryNameAndPredictedAmount(categoryName, predictedAmount);

                    return DailyBudgetResponseDto.TomorrowScheduleDto.builder()
                            .scheduleId(schedule.getScheduleId())
                            .title(schedule.getTitle())
                            .categoryName(categoryName.getDescription())
                            .predictedAmount(predictedAmount)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("내일 일정 매핑 완료. 총 항목 수: {}", tomorrowScheduleDtos.size());

        // 총 예산 금액 계산
        BigDecimal totalPredictedAmount = repeatedScheduleDtos.stream()
                .map(DailyBudgetResponseDto.RepeatedScheduleDto::getPredictedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalPredictedAmount = totalPredictedAmount.add(tomorrowScheduleDtos.stream()
                .map(DailyBudgetResponseDto.TomorrowScheduleDto::getPredictedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal budgetAmount = totalPredictedAmount;
        log.info("총 예산 금액 계산 완료: {}", budgetAmount);

        return DailyBudgetResponseDto.builder()
                .repeatedSchedules(repeatedScheduleDtos)
                .tomorrowSchedules(tomorrowScheduleDtos)
                .budgetAmount(budgetAmount)
                .build();
    }
}

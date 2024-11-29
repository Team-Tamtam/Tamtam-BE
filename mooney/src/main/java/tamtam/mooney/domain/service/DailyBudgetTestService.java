//package tamtam.mooney.domain.service;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.stereotype.Service;
//import tamtam.mooney.domain.dto.response.DailyBudgetResponseDto;
//import tamtam.mooney.domain.entity.CategoryName;
//import tamtam.mooney.domain.entity.UserSchedule;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DailyBudgetTestService {
//
//    public DailyBudgetResponseDto mergeSchedulesWithJsonData2(
//            List<UserSchedule> repeatedSchedules,
//            List<UserSchedule> tomorrowSchedules,
//            String jsonResponse
//    ) throws Exception {
//        log.info("mergeSchedulesWithJsonData 메소드 시작. jsonResponse: {}", jsonResponse);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode rootNode = objectMapper.readTree(jsonResponse);
//
//        log.info("JSON 파싱 성공.");
//
//        // JSON에서 recurring_expenses와 scheduled_expenses 추출
//        List<Map<String, Object>> recurringExpenseData = objectMapper.convertValue(
//                rootNode.get("recurring_expenses"),
//                new TypeReference<>() {}
//        );
//        List<Map<String, Object>> scheduledExpenseData = objectMapper.convertValue(
//                rootNode.get("scheduled_expenses"),
//                new TypeReference<>() {}
//        );
//
//        log.info("recurring_expenses 크기: {}, scheduled_expenses 크기: {}", recurringExpenseData.size(), scheduledExpenseData.size());
//
//        // 반복 소비 데이터 매핑
//        List<DailyBudgetResponseDto.RepeatedScheduleDto> repeatedScheduleDtos = recurringExpenseData.stream()
//                .map(expense -> {
//                    String category = (String) expense.get("category");
//                    BigDecimal perDayAmount = new BigDecimal(String.valueOf(expense.get("per_day_amount")));
//
//                    return new DailyBudgetResponseDto.RepeatedScheduleDto(
//                            null,
//                            category,
//                            category,
//                            perDayAmount
//                    );
//                })
//                .collect(Collectors.toList());
//
//        log.info("반복 일정 매핑 완료. 총 항목 수: {}", repeatedScheduleDtos.size());
//
//        // 내일 일정 데이터 매핑
//        List<DailyBudgetResponseDto.TomorrowScheduleDto> tomorrowScheduleDtos = tomorrowSchedules.stream()
//                .filter(schedule -> repeatedSchedules.stream()
//                        .noneMatch(repeated -> repeated.getScheduleId().equals(schedule.getScheduleId())))
//                .map(schedule -> {
//                    Map<String, Object> jsonExpense = scheduledExpenseData.stream()
//                            .filter(data -> schedule.getTitle().equals(data.get("description")))
//                            .findFirst()
//                            .orElse(null);
//
//                    CategoryName categoryName = Arrays.stream(CategoryName.values())
//                            .filter(c -> c.getDescription().equals(Objects.requireNonNull(jsonExpense).get("category")))
//                            .findFirst()
//                            .orElse(CategoryName.EXTRA);
//
//                    BigDecimal predictedAmount = (jsonExpense != null && jsonExpense.get("weighted_budget") != null)
//                            ? new BigDecimal(String.valueOf(jsonExpense.get("weighted_budget")))
//                            : schedule.getPredictedAmount();
//                    schedule.setCategoryNameAndPredictedAmount(categoryName, predictedAmount);
//
//                    return DailyBudgetResponseDto.TomorrowScheduleDto.builder()
//                            .scheduleId(schedule.getScheduleId())
//                            .title(schedule.getTitle())
//                            .categoryName(categoryName.getDescription())
//                            .predictedAmount(predictedAmount)
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        log.info("내일 일정 매핑 완료. 총 항목 수: {}", tomorrowScheduleDtos.size());
//
//        // 전체 예산 계산
//        BigDecimal totalPredictedAmount = repeatedScheduleDtos.stream()
//                .map(DailyBudgetResponseDto.RepeatedScheduleDto::getPredictedAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        totalPredictedAmount = totalPredictedAmount.add(tomorrowScheduleDtos.stream()
//                .map(DailyBudgetResponseDto.TomorrowScheduleDto::getPredictedAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add));
//
//        BigDecimal budgetAmount = totalPredictedAmount;
//        log.info("총 예산 금액 계산 완료: {}", budgetAmount);
//
//        return DailyBudgetResponseDto.builder()
//                .repeatedSchedules(repeatedScheduleDtos)
//                .tomorrowSchedules(tomorrowScheduleDtos)
//                .budgetAmount(budgetAmount)
//                .build();
//    }
//}
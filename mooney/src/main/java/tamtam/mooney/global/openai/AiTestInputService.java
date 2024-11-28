package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tamtam.mooney.global.openai.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiTestInputService {
    private final AIPromptService aiPromptService;

    public String buildDailyBudgetWithRequestBody(DailyBudgetInputRequestDto requestDto) {
        // 1. 카테고리별 반복되는 예산
        RecurringExpenseInputDto recurringExpense = requestDto.recurringExpense();
        Map<String, Object> recurringExpenseMap = Map.of(
                "category", recurringExpense.category(),
                "remaining_budget", recurringExpense.remainingBudget(),
                "remaining_days", recurringExpense.remainingDays(),
                "per_day_amount", recurringExpense.perDayAmount(),
                "per_meal_amount", recurringExpense.perMealAmount()
        );

        // 2. 예정된 소비 일정 설정
        List<ScheduledExpenseInputDto> scheduledExpenses = requestDto.scheduledExpenses();
        List<Map<String, Object>> scheduledExpensesMapList = scheduledExpenses.stream()
                .map(expense -> {
                    Map<String, Object> expenseMap = new HashMap<>();
                    expenseMap.put("time", expense.time());
                    expenseMap.put("description", expense.description());
                    expenseMap.put("category", expense.category());
                    expenseMap.put("category_budget", expense.categoryBudget());
                    expenseMap.put("weighted_budget", expense.weightedBudget());
                    expenseMap.put("per_event_budget", expense.perEventBudget());
                    expenseMap.put("average_price_suggestion", expense.averagePriceSuggestion());
                    expenseMap.put("remaining_events_in_category", expense.remainingEventsInCategory());
                    return expenseMap;
                })
                .toList();

        // 3. 전체 예산 설정
        double totalBudget = requestDto.totalBudget();

        // 4. 카테고리 가중치 설정
        double weightForCategory = requestDto.weightForCategory();

        // 5. buildDailyBudgetMessage 호출
        return aiPromptService.buildDailyBudgetMessage(recurringExpenseMap, scheduledExpensesMapList, totalBudget, weightForCategory);
    }

    public String buildMonthlyReportWithRequestBody(MonthlyReportInputRequestDto requestDto) {
        // 1. 전체 예산
        double totalBudget = requestDto.totalBudget();

        // 2. 카테고리별 예산
        Map<String, Integer> categoryBudgets = requestDto.categoryBudgets();
        List<MonthlyExpenseInputDto> monthlyExpenses = requestDto.monthlyExpenses();

        // 3. 이번 달 소비
        List<Map<String, Object>> monthlyExpensesMapList = monthlyExpenses.stream()
                .map(expense -> {
                    Map<String, Object> expenseMap = new HashMap<>();
                    expenseMap.put("date", expense.date());
                    expenseMap.put("category", expense.category());
                    expenseMap.put("description", expense.description());
                    expenseMap.put("amount", expense.amount());
                    return expenseMap;
                })
                .toList();

        // 3. buildMonthlyReportMessage 호출
        return aiPromptService.buildMonthlyReportMessage(totalBudget, categoryBudgets, monthlyExpensesMapList);
    }

    public String buildMonthlyBudgetWithRequestBody(MonthlyBudgetInputRequestDto requestDto, String message) {
        // 데이터 접근
        Map<String, Integer> currentBudget = requestDto.currentBudget();
        String feedbackMessage = requestDto.feedbackMessage();

        // FixedExpenses 변환 (List<FixedExpenseInputDto> -> List<Map<String, Object>>)
        List<Map<String, Object>> fixedExpenses = requestDto.fixedExpenses().stream()
                .map(fixedExpense -> {
                    Map<String, Object> expenseMap = new HashMap<>();
                    expenseMap.put("category", fixedExpense.category());
                    expenseMap.put("amount", fixedExpense.amount());
                    return expenseMap;
                })
                .collect(Collectors.toList());

        String userOpinions = requestDto.userOpinions();

        // NextMonthSchedules 변환 (List<NextMonthScheduleInputDto> -> List<Map<String, Object>>)
        List<Map<String, Object>> nextMonthSchedules = requestDto.nextMonthSchedules().stream()
                .map(schedule -> {
                    Map<String, Object> scheduleMap = new HashMap<>();
                    scheduleMap.put("event", schedule.event());
                    scheduleMap.put("estimated_cost", schedule.estimatedCost());
                    return scheduleMap;
                })
                .collect(Collectors.toList());

        double totalBudget = requestDto.totalBudget();

        // 기존 메서드 호출
        return aiPromptService.buildMonthlyBudgetMessage(
                currentBudget,
                feedbackMessage,
                fixedExpenses,
                userOpinions,
                nextMonthSchedules,
                totalBudget
        );
    }

    /*public String buildDailyBudgetWithTestData() {
        // 1. 카테고리별 반복되는 예산 설정
        Map<String, Object> recurringExpense = Map.of(
                "category", "식비",
                "remaining_budget", 25000,
                "remaining_days", 5,
                "per_day_amount", 5000.0,
                "per_meal_amount", 1666.67 // 3끼 기준
        );

        // 2. 예정된 소비 일정 설정
        List<Map<String, Object>> scheduledExpenses = new ArrayList<>();
        scheduledExpenses.add(Map.of(
                "time", "2024-11-12T07:16:02",
                "description", "졸프 애들이랑 이태원",
                "category", "문화/여가",
                "category_budget", 70000,
                "weighted_budget", 35000,
                "per_event_budget", 17500,
                "average_price_suggestion", 18000,
                "remaining_events_in_category", 2
        ));
        scheduledExpenses.add(Map.of(
                "time", "2024-11-15T20:00:00",
                "description", "지민재현이랑 익선동 카페",
                "category", "카페/간식",
                "category_budget", 15000,
                "weighted_budget", 7500,
                "per_event_budget", 7500,
                "average_price_suggestion", 8000,
                "remaining_events_in_category", 1
        ));

        // 3. 전체 예산 설정
        double totalBudget = 350000;

        // 4. 카테고리 가중치 설정
        double weightForCategory = 0.5;

        // 5. buildDailyBudgetMessage 호출
        return aiPromptService.buildDailyBudgetMessage(recurringExpense, scheduledExpenses, totalBudget, weightForCategory);
    }*/

    /*public String buildMonthlyReportWithTestData() {
        double totalBudget = 800000;

        Map<String, Integer> categoryBudgets = new HashMap<>();
        categoryBudgets.put("경조/선물", 0);
        categoryBudgets.put("교육/학습", 32000);
        categoryBudgets.put("교통", 80000);
        categoryBudgets.put("금융", 0);
        categoryBudgets.put("문화/여가", 120000);
        categoryBudgets.put("반려동물", 0);
        categoryBudgets.put("뷰티/미용", 40000);
        categoryBudgets.put("생활", 40000);
        categoryBudgets.put("술/유흥", 24000);
        categoryBudgets.put("식비", 240000);
        categoryBudgets.put("여행/숙박", 0);
        categoryBudgets.put("온라인 쇼핑", 0);
        categoryBudgets.put("의료/건강", 56000);
        categoryBudgets.put("자녀/육아", 0);
        categoryBudgets.put("자동차", 0);
        categoryBudgets.put("주거/통신", 0);
        categoryBudgets.put("카페/간식", 64000);
        categoryBudgets.put("패션/쇼핑", 24000);

        // 3. 월별 지출(monthlyExpenses) 리스트 생성
        List<Map<String, Object>> monthlyExpenses = new ArrayList<>();

        // 지출 데이터 추가
        monthlyExpenses.add(Map.of("date", "2024-11-01", "category", "식비", "description", "점심 식사", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-01", "category", "교통", "description", "지하철 이용", "amount", 2500));
        monthlyExpenses.add(Map.of("date", "2024-11-02", "category", "카페/간식", "description", "커피와 디저트", "amount", 8000));
        monthlyExpenses.add(Map.of("date", "2024-11-02", "category", "문화/여가", "description", "영화 관람", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-03", "category", "쇼핑", "description", "의류 구매", "amount", 35000));
        monthlyExpenses.add(Map.of("date", "2024-11-04", "category", "식비", "description", "저녁 외식", "amount", 20000));
        monthlyExpenses.add(Map.of("date", "2024-11-05", "category", "뷰티/미용", "description", "헤어 컷", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-06", "category", "교통", "description", "버스 이용", "amount", 1500));
        monthlyExpenses.add(Map.of("date", "2024-11-07", "category", "식비", "description", "점심 도시락", "amount", 8000));
        monthlyExpenses.add(Map.of("date", "2024-11-08", "category", "문화/여가", "description", "콘서트 티켓", "amount", 50000));
        monthlyExpenses.add(Map.of("date", "2024-11-09", "category", "의료/건강", "description", "약국 구매", "amount", 7000));
        monthlyExpenses.add(Map.of("date", "2024-11-10", "category", "카페/간식", "description", "브런치 카페", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-11", "category", "교통", "description", "택시 이용", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-12", "category", "식비", "description", "저녁 회식", "amount", 35000));
        monthlyExpenses.add(Map.of("date", "2024-11-13", "category", "생활", "description", "생필품 구매", "amount", 20000));
        monthlyExpenses.add(Map.of("date", "2024-11-14", "category", "쇼핑", "description", "책 구매", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-15", "category", "카페/간식", "description", "아이스크림", "amount", 5000));
        monthlyExpenses.add(Map.of("date", "2024-11-16", "category", "식비", "description", "주말 저녁 외식", "amount", 25000));
        monthlyExpenses.add(Map.of("date", "2024-11-17", "category", "교통", "description", "기차 이용", "amount", 45000));
        monthlyExpenses.add(Map.of("date", "2024-11-18", "category", "패션/쇼핑", "description", "신발 구매", "amount", 80000));
        monthlyExpenses.add(Map.of("date", "2024-11-19", "category", "식비", "description", "아침 식사", "amount", 5000));
        monthlyExpenses.add(Map.of("date", "2024-11-20", "category", "문화/여가", "description", "미술관 관람", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-21", "category", "교육/학습", "description", "토익 교재", "amount", 20000));
        monthlyExpenses.add(Map.of("date", "2024-11-22", "category", "카페/간식", "description", "커피", "amount", 4000));
        monthlyExpenses.add(Map.of("date", "2024-11-23", "category", "식비", "description", "점심 외식", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-24", "category", "술/유흥", "description", "친구들과 술자리", "amount", 45000));
        monthlyExpenses.add(Map.of("date", "2024-11-25", "category", "생활", "description", "청소 용품 구매", "amount", 10000));
        monthlyExpenses.add(Map.of("date", "2024-11-26", "category", "의료/건강", "description", "정기 검진", "amount", 30000));
        monthlyExpenses.add(Map.of("date", "2024-11-27", "category", "패션/쇼핑", "description", "액세서리 구매", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-28", "category", "식비", "description", "야식", "amount", 8000));
        monthlyExpenses.add(Map.of("date", "2024-11-29", "category", "카페/간식", "description", "디저트 카페", "amount", 7000));
        monthlyExpenses.add(Map.of("date", "2024-11-30", "category", "문화/여가", "description", "연극 관람", "amount", 30000));

        return aiPromptService.buildMonthlyReportMessage(totalBudget, categoryBudgets, monthlyExpenses);
    }*/

    /*public String buildMonthlyBudgetWithTestData(String userMessage) {
        // 1. 현재 예산 데이터 (카테고리별 예산)
        Map<String, Integer> currentBudget = new HashMap<>();
        currentBudget.put("경조/선물", 0);
        currentBudget.put("교육/학습", 300000);
        currentBudget.put("교통", 70000);
        currentBudget.put("금융", 0);
        currentBudget.put("문화/여가", 30000);
        currentBudget.put("반려동물", 0);
        currentBudget.put("뷰티/미용", 0);
        currentBudget.put("생활", 150000);
        currentBudget.put("술/유흥", 0);
        currentBudget.put("식비", 250000);
        currentBudget.put("여행/숙박", 0);
        currentBudget.put("온라인 쇼핑", 0);
        currentBudget.put("의료/건강", 0);
        currentBudget.put("자녀/육아", 0);
        currentBudget.put("자동차", 0);
        currentBudget.put("주거/통신", 150000);
        currentBudget.put("카페/간식", 20000);
        currentBudget.put("패션/쇼핑", 0);

        // 2. 이번 달 피드백 메시지
        String feedbackMessage = "이번 달 예산을 잘 지키셨습니다. 특히 식비 항목에서 예산을 초과하지 않았고, 카페/간식도 적절하게 사용하셨어요.";

        // 3. 고정 지출 데이터
        List<Map<String, Object>> fixedExpenses = new ArrayList<>();
        Map<String, Object> rentExpense = new HashMap<>();
        rentExpense.put("category", "주거/통신");
        rentExpense.put("amount", 150000);
        fixedExpenses.add(rentExpense);

        Map<String, Object> transportExpense = new HashMap<>();
        transportExpense.put("category", "교통");
        transportExpense.put("amount", 70000);
        fixedExpenses.add(transportExpense);

        // 4. 사용자 의견 (다음 달 계획)
        String userOpinions = userMessage;

        // 5. 다음 달 일정 (특별 지출 계획)
        List<Map<String, Object>> nextMonthSchedules = new ArrayList<>();
        Map<String, Object> holidayPlan = new HashMap<>();
        holidayPlan.put("event", "여행");
        holidayPlan.put("estimated_cost", 150000);
        nextMonthSchedules.add(holidayPlan);

        // 6. 총 예산
        double totalBudget = 800000;

        // 7. buildMonthlyBudgetMessage 메소드 호출
        return aiPromptService.buildMonthlyBudgetMessage(
                currentBudget,
                feedbackMessage,
                fixedExpenses,
                userOpinions,
                nextMonthSchedules,
                totalBudget
        );
    }*/
}

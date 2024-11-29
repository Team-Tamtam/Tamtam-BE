package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.request.MonthlyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.MonthlyBudgetResponseDto;
import tamtam.mooney.domain.entity.MonthlyBudget;
import tamtam.mooney.domain.entity.User;
import tamtam.mooney.domain.repository.MonthlyBudgetRepository;
import tamtam.mooney.global.openai.AIPromptService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final UserService userService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final AIPromptService aiPromptService;

    public MonthlyBudgetResponseDto createNextMonthBudget(MonthlyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        Map<String, Integer> currentBudget = new HashMap<>();

        currentBudget.put("교육/학습", 300000);
        currentBudget.put("교통", 70000);
        currentBudget.put("금융", 50000);
        currentBudget.put("문화/여가", 30000);
        currentBudget.put("뷰티/미용", 50000);
        currentBudget.put("생활", 100000);
        currentBudget.put("식비", 200000);
        currentBudget.put("카페/간식", 25000);
        currentBudget.put("패션/쇼핑", 15000);
        currentBudget.put("의료/건강", 30000);
        currentBudget.put("주거/통신", 150000);
        currentBudget.put("경조/선물", 0);
        currentBudget.put("자녀/육아", 0);
        currentBudget.put("자동차", 0);
        currentBudget.put("여행/숙박", 0);
        currentBudget.put("온라인 쇼핑", 0);
        currentBudget.put("술/유흥", 20000);

        // List<Map<String, Object>> 생성
        List<Map<String, Object>> fixedExpenses = new ArrayList<>();

        // 첫 번째 항목: 넷플릭스 구독료
        Map<String, Object> expense1 = new HashMap<>();
        expense1.put("name", "넷플릭스 구독료");
        expense1.put("amount", 5500);
        expense1.put("frequency", "monthly");
        expense1.put("type", "fixed");
        expense1.put("description", "넷플릭스 스트리밍 서비스 구독료");

        // 두 번째 항목: 인터넷
        Map<String, Object> expense2 = new HashMap<>();
        expense2.put("name", "멜론 구독료");
        expense2.put("amount", 7900);
        expense2.put("frequency", "monthly");
        expense2.put("type", "fixed");
        expense2.put("description", "멜론 스트리밍 서비스 구독료");

        // 리스트에 항목 추가
        fixedExpenses.add(expense1);
        fixedExpenses.add(expense2);

        List<Map<String, Object>> nextMonthSchedules = new ArrayList<>();

        // 첫 번째 일정: 친구들이랑 파티
        Map<String, Object> schedule1 = new HashMap<>();
        schedule1.put("description", "친구들이랑 파티");
        schedule1.put("time", "2024-12-15 00:00:00");
        schedule1.put("category", "여행/숙박");

        // 두 번째 일정: TOEIC 시험 준비 (온라인)
        Map<String, Object> schedule2 = new HashMap<>();
        schedule2.put("description", "TOEIC 시험 준비 (온라인)");
        schedule2.put("time", "2024-12-05 10:00:00");
        schedule2.put("category", "교육/학습");

        // 리스트에 일정 추가
        nextMonthSchedules.add(schedule1);
        nextMonthSchedules.add(schedule2);

        String aiResponse = aiPromptService.buildMonthlyBudgetMessage(
                currentBudget,
        "이번 달 예산을 비교적 잘 지켰지만, 식비는 조금 초과했습니다. 카페/간식은 적당히 사용했고, 패션/쇼핑 항목은 거의 사용하지 않았습니다. 다음 달에는 여행이 계획되어 있으니 여행 예산을 확보하는 것이 필요합니다.",
                fixedExpenses,
                requestDto.message(),
                nextMonthSchedules,
                requestDto.budgetAmount()
        );

        return parseBudgetResponse(aiResponse, nextMonthSchedules);
    }


    public static MonthlyBudgetResponseDto parseBudgetResponse(
            String inputJson,
            List<Map<String, Object>> nextMonthSchedules
    ) {
        JSONObject input = new JSONObject(inputJson);

        // MonthlyBudgetResponseDto 객체 생성
        MonthlyBudgetResponseDto responseDto = new MonthlyBudgetResponseDto();

        // 1. 카테고리별 예산 (category_budgets)
        JSONObject categoryBudgets = input.getJSONObject("monthly_budget").getJSONObject("category_budgets");
        List<MonthlyBudgetResponseDto.CategoryBudgetDto> categories = new ArrayList<>();

        // totalBudget 계산
        BigDecimal totalBudget = BigDecimal.ZERO; // 초기값은 0으로 설정

        // 모든 카테고리 항목에 대해 값을 확인하고, 값이 0이 아닌 항목만 추가
        Iterator<String> keys = categoryBudgets.keys();
        while (keys.hasNext()) {
            String categoryName = keys.next();
            BigDecimal categoryBudgetAmount = categoryBudgets.getBigDecimal(categoryName);

            // 값이 0보다 크면 카테고리에 추가
            if (categoryBudgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                MonthlyBudgetResponseDto.CategoryBudgetDto categoryDto = new MonthlyBudgetResponseDto.CategoryBudgetDto();
                categoryDto.setCategoryName(categoryName);
                categoryDto.setCategoryBudgetAmount(categoryBudgetAmount);
                categories.add(categoryDto);

                // totalBudget 계산
                totalBudget = totalBudget.add(categoryBudgetAmount);
            }
        }
        responseDto.setCategories(categories);

        // 2. 총 예산 (total_budget) 설정
        responseDto.setBudgetAmount(totalBudget);


        // 3. 중요 일정

        // MonthScheduleDto로 변환
        List<MonthlyBudgetResponseDto.MonthScheduleDto> keySchedules = new ArrayList<>();
        for (Map<String, Object> schedule : nextMonthSchedules) {
            MonthlyBudgetResponseDto.MonthScheduleDto scheduleDto = MonthlyBudgetResponseDto.MonthScheduleDto.builder()
                    .scheduleId(System.currentTimeMillis())  // 임의로 scheduleId 부여, 실제 환경에서는 적절한 값 사용
                    .title((String) schedule.get("description"))
                    .categoryName((String) schedule.get("category"))
                    .startDateTime((String) schedule.get("time"))  // 시간 형식에 맞게 변환 필요
                    .endDateTime((String) schedule.get("time"))   // 시간 형식에 맞게 변환 필요
                    .build();
            keySchedules.add(scheduleDto);
            responseDto.setKeySchedules(keySchedules);
        }

        // 4. 이유 (reason)
        String reason = input.getString("reason");
        responseDto.setReason(reason);

        return responseDto;
    }

    private MonthlyBudgetResponseDto.MonthScheduleDto createScheduleDto(Long scheduleId, String title, String categoryName, String startDateTime, String endDateTime) {
        MonthlyBudgetResponseDto.MonthScheduleDto dto = new MonthlyBudgetResponseDto.MonthScheduleDto();
        dto.setScheduleId(scheduleId);
        dto.setTitle(title);
        dto.setCategoryName(categoryName);
        dto.setStartDateTime(startDateTime);
        dto.setEndDateTime(endDateTime);
        return dto;
    }

    // 다음 달의 기간 반환
    private String getNextMonthPeriod() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonth = now.plusMonths(1);
        return nextMonth.getYear() + "-" + String.format("%02d", nextMonth.getMonthValue());
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyBudgetAmount(User user, String period) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByUserAndPeriod(user, period)
                .orElseThrow(() -> new IllegalArgumentException("해당 월에 대한 예산 정보가 없습니다."));
        return monthlyBudget.getFinalAmount() != null ? monthlyBudget.getFinalAmount() : monthlyBudget.getInitialAmount();
    }
}

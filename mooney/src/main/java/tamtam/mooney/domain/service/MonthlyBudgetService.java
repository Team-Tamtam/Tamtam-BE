package tamtam.mooney.domain.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.dto.request.MonthlyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.CategoryExpenseResponseDto;
import tamtam.mooney.domain.dto.response.MonthlyBudgetResponseDto;
import tamtam.mooney.domain.entity.*;
import tamtam.mooney.domain.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.repository.MonthlyReportRepository;
import tamtam.mooney.global.openai.AIPromptService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final UserService userService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final MonthlyReportRepository monthlyReportRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final AIPromptService aiPromptService;

    public MonthlyBudgetResponseDto createNextMonthBudget(MonthlyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // 현재 월의 예산을 데이터베이스에서 가져오기
        String currentPeriod = "2024-11"; // 현재 기간을 동적으로 계산하거나 requestDto에서 가져올 수 있습니다.
        List<CategoryBudget> currentBudgetList = categoryBudgetRepository.findByUserAndPeriod(user, currentPeriod);

        // 예산 데이터를 저장할 Map 생성
        Map<String, Integer> currentBudget = new HashMap<>();

        // 데이터베이스에서 가져온 예산 정보를 currentBudget Map에 채우기
        for (CategoryBudget categoryBudget : currentBudgetList) {
            // CategoryBudget의 카테고리 이름을 문자열로 변환하여 사용
            String categoryName = categoryBudget.getCategoryName().toString();
            // 예산은 BigDecimal에서 Integer로 변환하여 저장
            currentBudget.put(categoryName, categoryBudget.getAmount().intValue());
        }

        // 만약 예산 데이터가 비어있다면 기본값을 할당
        if (currentBudget.isEmpty()) {
            currentBudget.put("교육/학습", 200000);
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
        }
        String feedbackMessage;
        Optional<MonthlyReport> monthlyReport = monthlyReportRepository.findByUserAndPeriod(user, currentPeriod);

        if (monthlyReport.isPresent())
            feedbackMessage = monthlyReport.get().getAgentComment();
        else
            feedbackMessage = "";

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

        // 첫 번째 일정: 친구들이랑 여행
        Map<String, Object> schedule1 = new HashMap<>();
        schedule1.put("description", "친구들이랑 여행");
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

        //  Map<String, BigDecimal> categoryBudgets,Map<String, BigDecimal> categoryExpenses 사용해서 사용자가 덜 쓴만큼 반영한 다음 달 예산을 초기화하고 ai에게 넘기게 해야될듯
        // 소비 금액 계산


        String aiResponse = aiPromptService.buildMonthlyBudgetMessage(
                currentBudget,
                feedbackMessage,
                fixedExpenses,
                requestDto.message(),
                nextMonthSchedules,
                requestDto.budgetAmount()
        );

        return parseBudgetResponse(aiResponse, nextMonthSchedules, requestDto.budgetAmount());
    }


    public static MonthlyBudgetResponseDto parseBudgetResponse(
            String inputJson,
            List<Map<String, Object>> nextMonthSchedules,
            BigDecimal userRequestedBudget
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
        BigDecimal difference = userRequestedBudget.subtract(totalBudget);

        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            // CategoryName.EXTRA의 categoryDto를 setCategoryBudgetAmount로 설정
            MonthlyBudgetResponseDto.CategoryBudgetDto extraCategoryDto =  new MonthlyBudgetResponseDto.CategoryBudgetDto();
            extraCategoryDto.setCategoryName(CategoryName.EXTRA.getDescription());
            extraCategoryDto.setCategoryBudgetAmount(difference);
            categories.add(extraCategoryDto);
            totalBudget = userRequestedBudget;
        }
        responseDto.setCategories(categories);
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

    public Object createNextMonthBudget2(@Valid MonthlyBudgetRequestDto requestDto) {
        return "{\n" +
                "  \"budgetAmount\": 800000,\n" +
                "  \"categories\": [\n" +
                "    {\n" +
                "      \"categoryName\": \"식비\",\n" +
                "      \"categoryBudgetAmount\": 350000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"여행/숙박\",\n" +
                "      \"categoryBudgetAmount\": 100000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"문화/여가\",\n" +
                "      \"categoryBudgetAmount\": 80000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"교통\",\n" +
                "      \"categoryBudgetAmount\": 70000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"패션/쇼핑\",\n" +
                "      \"categoryBudgetAmount\": 50000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"교육/학습\",\n" +
                "      \"categoryBudgetAmount\": 50000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"뷰티/미용\",\n" +
                "      \"categoryBudgetAmount\": 40000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"카페/간식\",\n" +
                "      \"categoryBudgetAmount\": 25000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"의료/건강\",\n" +
                "      \"categoryBudgetAmount\": 30000\n" +
                "    },\n" +
                "    {\n" +
                "      \"categoryName\": \"기타\",\n" +
                "      \"categoryBudgetAmount\": 5000\n" +
                "    }\n" +
                "  ],\n" +
                "  \"keySchedules\": [\n" +
                "    {\n" +
                "      \"scheduleId\": 1732969707658,\n" +
                "      \"title\": \"친구들이랑 여행\",\n" +
                "      \"categoryName\": \"여행/숙박\",\n" +
                "      \"startDateTime\": \"2024-12-15 00:00:00\",\n" +
                "      \"endDateTime\": \"2024-12-15 00:00:00\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"scheduleId\": 1732969707658,\n" +
                "      \"title\": \"TOEIC 시험 준비 (온라인)\",\n" +
                "      \"categoryName\": \"교육/학습\",\n" +
                "      \"startDateTime\": \"2024-12-05 10:00:00\",\n" +
                "      \"endDateTime\": \"2024-12-05 10:00:00\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"reason\": \"화연님의 다음 달 예산을 mooney가 짜보았습니다. \uD83D\uDE0A 이번 달에 교육/학습과 뷰티/미용에서 예산을 잘 지키셨기에, 다음 달에는 이 두 카테고리를 각각 50000원과 40000원으로 조정했습니다. \uD83D\uDCA1 특히 교육/학습 카테고리는 TOEIC 시험 준비를 위해 조금 더 예산을 늘렸습니다. 여행/숙박 카테고리에도 50000원을 배정하여 친구들과의 여행을 준비할 수 있도록 하였습니다. \uD83C\uDFD6\uFE0F \\n\\n하지만 지난달 문화/여가에서 예산을 초과하셨기에, 다음 달에는 해당 카테고리를 80000원으로 조정하여 지출을 관리해보실 수 있도록 했습니다. 또한 패션/쇼핑 카테고리도 예산을 줄여 50000원으로 설정했습니다. \uD83D\uDC57 이는 필요에 따라 더 많은 저축으로 이어질 수 있을 거예요. \uD83D\uDE0A \\n\\n식비는 사용자의 의견을 반영하여 350000원으로 늘렸습니다. 외식 비용을 줄이시는 것에 대한 조언을 고려하여, 식비를 늘려 다양한 음식을 즐길 수 있도록 했습니다. \uD83C\uDF7D\uFE0F 교통비는 지난달과 동일하게 유지했습니다. \\n\\n마지막으로 카페/간식 예산도 그대로 25000원으로 유지하여 자주 가는 카페에서 여유를 즐기는 데 도움이 될 것입니다. ☕ 이번 달 예산이 화연님께 도움이 되길 바랍니다! \uD83C\uDF89\"\n" +
                "}\n";
    }
}

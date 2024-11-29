package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIPromptService {
    private final OpenAIService openAIService;

    /**
     * 기능 1 - 이번 달 예산과 소비를 분석하여 일일 예산을 계산하고 결과 메시지를 만듭니다.
     * @param recurringExpense 식비와 같은 반복적인 카테고리 예산 (Map 형식)
     * @param scheduledExpenses 특정 일정에 대한 소비 예산 (List 형식)
     * @param totalBudget 전체 예산 (double 형식)
     * @param weightForCategory 카테고리별 예산 계산에 사용할 가중치 (double 형식)
     * @return 계산된 일일 예산을 포함한 피드백 메시지 내용이 담긴 String
     */
    public String buildDailyBudgetMessage(
            Map<String, Object> recurringExpense,
            List<Map<String, Object>> scheduledExpenses,
            double totalBudget,
            double weightForCategory
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        final String GPT_PROMPT = "You are a budget assistant designed to help users calculate their daily budget based on recurring expenses and scheduled events. Your task is to: \n" +
                "1) Calculate the daily budget for recurring expenses, dividing the remaining budget by the number of days left. Additionally, calculate a per-meal budget for the recurring '식비' category assuming 3 meals per day. \n" +
                "2) For scheduled expenses, divide the remaining budget of the event's category by the number of events in that category. Use external price suggestions to adjust the event budget. \n" +
                "3) Combine the recurring expense daily budget with the scheduled event budgets to estimate the total daily budget. Ensure all calculations are accurate and easy to understand. \n" +
                "4) Use the provided weight for each event and category to balance the calculations.";

        // 메시지를 저장할 JSON 배열 생성
        JSONArray messages = new JSONArray();

        // system 메시지
        JSONObject systemMessage = createSystemMessage(GPT_PROMPT);
        messages.put(systemMessage);

        // User Role 메시지 1
        JSONObject userMessage1 = new JSONObject();
        userMessage1.put("role", "user");
        JSONArray userContent1 = new JSONArray();
        JSONObject userContentObject1 = new JSONObject();
        userContentObject1.put("type", "text");
        userContentObject1.put("text", "I am providing budget data for the assistant to calculate my daily budget for November 11, 2024. My goal is to divide expenses between recurring categories (like meals) and specific scheduled events. Here’s a detailed breakdown: Recurring Expense: 식비 The \"식비\" category has a remaining budget of ₩250,000, and there are 20 days left in the month. Please divide this budget evenly across the remaining days and calculate a per-meal amount, assuming three meals per day. Scheduled Expenses In addition to recurring expenses, there are specific scheduled events for which I need an expense estimate. Here's how to handle these: Event Categorization: Please categorize the event \"졸프 애들이랑 이태원\" appropriately if possible. Otherwise, provide an option for me to choose later. Budget Allocation: Each event should receive a budget by dividing the remaining budget of its category by the number of remaining events in that category. Example: \"교육/학습\" has a remaining budget of ₩10,000, and there is 1 event in this category. \"문화/여가\" has a remaining budget of ₩70,000, and there are 2 events in this category. Price Suggestions: Include external price suggestions (e.g., average costs for similar activities among people in their 20s) if available. Combine the calculated category-based budget with the external price suggestion using the specified weight. The default weight is 0.5 for each unless I specify otherwise.");
        userContent1.put(userContentObject1);
        userMessage1.put("content", userContent1);

        // User Role 메시지 2 (Budget Data)
        JSONObject userMessage2 = new JSONObject();
        userMessage2.put("role", "user");

        JSONArray userContent2 = new JSONArray();
        JSONObject userContentObject2 = new JSONObject();

        // 사용자의 예산 데이터를 JSON으로 구성
        JSONObject budgetData = new JSONObject()
                .put("weight_for_category", weightForCategory)
                .put("total_remaining_budget", totalBudget)
                .put("recurring_expense", new JSONObject(recurringExpense))
                .put("scheduled_expenses", new JSONArray(scheduledExpenses));

        userContentObject2.put("type", "text");
        userContentObject2.put("text", "Provided Data: \n" + budgetData);

        userContent2.put(userContentObject2);
        userMessage2.put("content", userContent2);

        // Assistant Role 메시지
        JSONObject assistantMessage = new JSONObject();
        assistantMessage.put("role", "assistant");
        JSONArray assistantContent = new JSONArray();
        JSONObject assistantContentObject = new JSONObject();
        assistantContentObject.put("type",  "Expectations 1. Calculate the total daily budget by combining the recurring \"식비\" budget and event-specific budgets. 2. Clearly separate calculations for each category and allow me to adjust the weights or budgets if needed. 3. Ensure all calculations are accurate, easy to understand, and modifiable.");
        assistantContent.put(assistantContentObject);
        assistantMessage.put("content", assistantContent);

        // 메시지 배열에 추가
        messages.put(userMessage1);
        messages.put(userMessage2);
        messages.put(assistantMessage);

        // response_format 필드 추가
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");

        // tools 필드 추가
        JSONObject functionDetails = new JSONObject();
        functionDetails.put("name", "TomorrowBudget");
        functionDetails.put("strict", false);

        JSONObject parameters = new JSONObject();
        parameters.put("type", "object");
        parameters.put("required", new JSONArray(List.of("recurring_expense", "scheduled_expenses", "daily_budget_total")));

        JSONObject recurringExpenseSchema = new JSONObject();
        recurringExpenseSchema.put("type", "object");
        recurringExpenseSchema.put("required", new JSONArray(List.of("category", "remaining_budget", "remaining_days")));
        recurringExpenseSchema.put("properties", new JSONObject()
                .put("category", new JSONObject().put("type", "string"))
                .put("per_day_amount", new JSONObject().put("type", "number"))
                .put("remaining_days", new JSONObject().put("type", "integer"))
                .put("per_meal_amount", new JSONObject().put("type", "number"))
                .put("remaining_budget", new JSONObject().put("type", "number")));

        JSONObject scheduledExpensesSchema = new JSONObject();
        scheduledExpensesSchema.put("type", "array");
        scheduledExpensesSchema.put("items", new JSONObject()
                .put("type", "object")
                .put("required", new JSONArray(List.of("time", "description", "category")))
                .put("properties", new JSONObject()
                        .put("time", new JSONObject().put("type", "string").put("format", "date-time"))
                        .put("category", new JSONObject().put("type", "string"))
                        .put("description", new JSONObject().put("type", "string"))
                        .put("category_budget", new JSONObject().put("type", "number"))
                        .put("weighted_budget", new JSONObject().put("type", "number"))
                        .put("per_event_budget", new JSONObject().put("type", "number"))
                        .put("average_price_suggestion", new JSONObject().put("type", "number"))
                        .put("remaining_events_in_category", new JSONObject().put("type", "integer"))));

        parameters.put("properties", new JSONObject()
                .put("recurring_expense", recurringExpenseSchema)
                .put("scheduled_expenses", scheduledExpensesSchema)
                .put("daily_budget_total", new JSONObject().put("type", "number")));

        functionDetails.put("parameters", parameters);

        JSONArray tools = new JSONArray();
        tools.put(new JSONObject().put("type", "function").put("function", functionDetails));

        // messages 배열에 response_format 및 tools 추가
        messages.put(new JSONObject().put("response_format", responseFormat));
        messages.put(new JSONObject().put("tools", tools));

        // OpenAI 서비스 호출 및 결과 반환
        return openAIService.generateGPTResponse(messages, 0.7, 2048, 1, 0, 0);
    }

    /**
     * 기능 2 - 월별 리포트에서 피드백 메시지를 생성하기 위한 메시지를 만듭니다.
     * @param totalBudgetAmount 전체 예산
     * @param categoryBudgets 카테고리별 예산 (Map 형식)
     * @param totalExpenseAmount 전체 지출
     * @param categoryExpenses 카테고리별 예산 (Map 형식)
     * @return 생성된 피드백 메시지 내용이 담긴 String
     */
    public String buildMonthlyReportMessage(
            BigDecimal totalBudgetAmount,
            Map<String, BigDecimal> categoryBudgets,
            BigDecimal totalExpenseAmount,
            Map<String, BigDecimal> categoryExpenses
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        final String GPT_PROMPT = "You are a financial assistant designed to analyze a user's monthly budget and expenses. Your task is to compare their planned budget with actual expenses, identify areas of success and improvement, and provide actionable, motivational feedback for the next month. Follow these rules:\\n\\n1) Start with positive feedback on total and move in to each categories where the user stayed within or under budget.\\n 2) Identify categories where the user exceeded their budget, explaining why it might have happened.\\n   - Example: \\\"Transportation costs were higher than planned, likely due to unexpected trips.\\\"\\n\\n3) Provide actionable suggestions to improve spending habits next month.\\n   - Example: \\\"Consider reducing dining out and reallocating the savings to other categories.\\\"\\n\\n4) Use friendly and motivational language to encourage the user.\\n\\n5) Output the response as a single JSON object under the key \\\"response\\\".6) You should only have to give feedback in Korean, not English. 7) You will end up giving one paragaph(3~4 sentences in Korean) of feedback which should include everything, not separtely. That's the only thing you should give it. TOTAL SUMMARY OF FEEDBACK OF THIS MONTH. 8) put 2 emojis in the middle of sentence and say in friendly tone. You should focus on more positive things and emphasis more that negative things. The example of expected response is like '이번 달 총 예산을 90% 사용하시면서 예산 내에 소비를 성공하셨네요!! 특히 식비를 정말 잘 절약하셨어요! \uD83C\uDF89 하지만 외식비가 예산을 살짝 초과한 점이 아쉬워요. 다음 달에는 외식 횟수를 조금 줄이고 대신 식비 예산을 살짝 늘려서 균형을 맞춰보는 건 어떨까요? 이렇게 하면 더 많은 저축도 가능할 거예요! \uD83D\uDE0A'";

        // 메시지를 저장할 JSON 배열 생성
        JSONArray messages = new JSONArray();

        // system 메시지
        JSONObject systemMessage = createSystemMessage(GPT_PROMPT);
        messages.put(systemMessage);



        // 사용자 메시지: 월별 예산 및 지출 데이터 입력
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        JSONObject userContent = new JSONObject();

        // 월별 지출 및 예산 데이터를 새 형식에 맞게 업데이트
        JSONObject monthlyData = new JSONObject();

        // 월별 지출 데이터 추가
        JSONObject expenseData = new JSONObject();
        expenseData.put("total_expense_amount", totalExpenseAmount);
        expenseData.put("category_expenses", new JSONObject(categoryExpenses));

        // 월별 예산 데이터 추가
        JSONObject budgetData = new JSONObject();
        budgetData.put("total_budget_amount", totalBudgetAmount);
        budgetData.put("category_budgets", new JSONObject(categoryBudgets));

        monthlyData.put("monthly_expenses", expenseData);
        monthlyData.put("monthly_budget", budgetData);

        // 사용자 메시지에 월별 데이터를 "text"로 추가
        userContent.put("type", "text");
        userContent.put("text", "Here is my monthly data: \n" + monthlyData);

        // 사용자 메시지에 content 추가
        userMessage.put("content", userContent);

        // 메시지 배열에 사용자 메시지 추가
        messages.put(userMessage);


        // OpenAI 서비스 호출 및 결과 반환
        return openAIService.generateGPTResponse(messages, 1.1, 200, 1, 0, 0);
    }


    /**
     * 사용자의 소비 데이터를 기반으로 다음 달 예산 플랜 메시지를 생성하는 메서드입니다.
     * 이 메서드는 OpenAI GPT 모델에 제공할 JSON 메시지를 생성하며,
     * 총 예산과 카테고리별 세부 예산을 포함합니다.
     *
     * @param currentBudget    현재 예산 데이터 (카테고리별 예산 정보)
     * @param feedbackMessage  사용자에게 제공된 소비 피드백 메시지
     * @param fixedExpenses    고정 지출 리스트 (항목명, 금액, 빈도, 유형, 설명 포함)
     * @param userOpinions     사용자의 의견 및 다음 달 계획
     * @param nextMonthSchedules 다음 달 일정 리스트 (일정 이름, 기간, 카테고리 포함)
     * @param totalBudget      다음 달 전체 예산 (총 금액)
     * @return OpenAI GPT 모델에 전달할 JSON 메시지
     */
    public String buildMonthlyBudgetMessage(
            Map<String, Integer> currentBudget,
            String feedbackMessage,
            List<Map<String, Object>> fixedExpenses,
            String userOpinions,
            List<Map<String, Object>> nextMonthSchedules,
            double totalBudget
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        final String GPT_PROMPT = "You are a financial assistant AI responsible for creating a personalized monthly budget. Your task is to analyze the provided data and generate a budget plan for the next month in JSON format. Ensure the budget is tailored for 30 days (one month). Assign categories reasonably, especially for essential costs like food and living expenses. Explain the reasoning behind your allocations in detail in Korean in the `reason` field. YOU SHOULD ASSING REASONABLE BUDGET FOR SCHEDULE AND EACH CATEGORIES. \n\n### Inputs:\n1. **This Month's Budget**: The budget allocated to each category this month. \n2. **This Month's Spending Feedback**: Feedback on whether the user overspent or underspent in various categories this month.\n3. **Fixed Expenses**: Non-negotiable expenses such as rent, utilities, or loan repayments that must be covered.\n4. **User's Preferences**: Specific preferences or priorities for the next month. \n   - If the user's preferences cannot be fully accommodated without exceeding the total budget or making other categories severely underfunded, partially adjust the allocations and explain the reasons clearly in the `reason` field. If you cannot make it, YOU SHOULD EXPLAIN IN THE RESON THO.\n5. **Next Month's Plans**: Include additional expenses for special events, holidays, or other significant activities. You should assign reasonable amount of cost. Especially if it's travel to somewhere, you should calculate all the flight and accomodations, and food, etc. \n6. **Total Budget**: The maximum budget available for allocation next month (e.g., 800,000 KRW).\n\n### Output Requirements:\nGenerate the budget in the following JSON format:\n```json\n{\n  \"monthly_budget\": {\n    \"total_budget\": 800000,\n    \"category_budgets\": {\n      \"경조/선물\": 0,\n      \"교육/학습\": 0,\n      \"교통\": 0,\n      \"금융\": 0,\n      \"문화/여가\": 0,\n      \"반려동물\": 0,\n      \"뷰티/미용\": 0,\n      \"생활\": 0,\n      \"술/유흥\": 0,\n      \"식비\": 0,\n      \"여행/숙박\": 0,\n      \"온라인 쇼핑\": 0,\n      \"의료/건강\": 0,\n      \"자녀/육아\": 0,\n      \"자동차\": 0,\n      \"주거/통신\": 0,\n      \"카페/간식\": 0,\n      \"패션/쇼핑\": 0\n    }\n  },\n  \"reason\": \"Explain the reasoning behind the budget allocations in Korean. Example of response is like the following, 이번 달 예산은 사용자님의 생활 패턴과 피드백을 꼼꼼히 반영해서 짜 보았어요. 먼저, 필수적으로 지출해야 하는 고정비(주거/통신 150,000원, 교통 70,000원)는 꼭 필요한 만큼 먼저 배정했어요. 다음달에는 지민님이 다음 달에는 기타 학원 등록을 원하셨기 때문에 교육/학습 항목에 300,000원을 충분히 배정했어요. 지난달 식비가 조금 초과되었다고 하셔서, 충분히 여유롭게 드실 수 있도록 260,000원으로 살짝 늘렸어요. 카페/간식은 딱 적당히 사용하신 것 같아 이번에도 그대로 유지했어요. 문화/여가 쪽은 사용이 적어서 30,000원을 줄이고, 그 금액은 다른 꼭 필요한 항목에 넣었답니다. 또한, 다음 달 주말 여행을 계획하셨다고 하셔서, 여행/숙박 항목에 150,000원을 새로 배정했어요. 요청하신 다른 항목들 중에서 예산에 부담이 되는 부분은 조금 줄였지만, 최대한 반영하려 노력했답니다. 술/유흥은 특별히 많이 필요하지 않으실 것 같아 조금 줄였고, 생활비는 꼭 필요한 만큼 그대로 유지했어요. 의료/건강 항목은 정기 검진 등 필요한 비용을 고려해서 10,000원을 추가했어요. 지난 달 피드백에 따라 쇼핑에서 지출을 줄여서 다음 달 주요 일정이 여행을 위한 돈을 모아보는 건 어떻까요? 전체 예산이 800,000원을 넘지 않도록 하나하나 신경 써서 조정했으니, 다음 달도 편안하게 보내실 수 있기를 바라요. 😊\" \n}\n";

        // 메시지를 저장할 JSON 배열 생성
        JSONArray messages = new JSONArray();

        // 시스템 메시지 추가
        JSONObject systemMessage = createSystemMessage(GPT_PROMPT);
        messages.put(systemMessage);

        // 사용자 데이터 입력 메시지 생성
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        JSONObject userData = new JSONObject();

        // 현재 예산
        userData.put("current_budget", new JSONObject()
                .put("monthly_budget", new JSONObject()
                        .put("total_budget", totalBudget)
                        .put("category_budgets", currentBudget)));

        // 이번 달 피드백
        userData.put("current_feedback", new JSONObject()
                .put("message", feedbackMessage));

        // 고정 지출
        JSONArray fixedExpensesArray = new JSONArray(fixedExpenses);
        userData.put("fixed_expenses", fixedExpensesArray);

        // 사용자 의견
        userData.put("user_opinions", new JSONObject()
                .put("plans", userOpinions));

        // 다음 달 일정
        JSONArray schedulesArray = new JSONArray(nextMonthSchedules);
        userData.put("next_month_schedules", schedulesArray);

        // 응답 포맷 추가
        userData.put("response_format", new JSONObject()
                .put("type", "json_object"));

        // 사용자 메시지에 데이터 추가
        userMessage.put("content", userData);
        messages.put(userMessage);

        // OpenAI 서비스 호출 및 결과 반환
        return openAIService.generateGPTResponse(messages, 1.0, 2048, 1, 0, 0);
    }

    /**
     * 시스템 메시지를 생성하는 메소드
     * @param prompt GPT 모델에 전달할 프롬프트 텍스트
     * @return 시스템 메시지
     */
    private JSONObject createSystemMessage(String prompt) {
        // 시스템 메시지에 사용할 contentObject 생성
        JSONObject systemContentObject = new JSONObject();
        systemContentObject.put("type", "text");
        systemContentObject.put("text", prompt);

        // 시스템 메시지 객체 생성
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", new JSONArray().put(systemContentObject));

        return systemMessage;
    }
}

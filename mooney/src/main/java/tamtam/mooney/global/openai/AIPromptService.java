package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIPromptService {
    private final OpenAIService openAIService;

    /**
     * 기능 1 - 이번 달 예산과 소비를 분석하여 일일 예산을 계산하고 결과 메시지를 만듭니다.
     * @param tomorrowDate 내일 날짜
     * @param scheduledExpenses 특정 일정에 대한 소비 예산 (List 형식)
     * @param totalBudget 전체 예산
     * @param weightForCategory 카테고리별 예산 계산에 사용할 가중치 (double 형식)
     * @return 계산된 일일 예산을 포함한 피드백 메시지 내용이 담긴 String
     */
    public String buildDailyBudgetMessage(
            LocalDate tomorrowDate,
            List<Map<String, Object>> scheduledExpenses,
            BigDecimal totalBudget,
            double weightForCategory
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        final String GPT_PROMPT = "You are a budget assistant designed to help users calculate their daily budget based on recurring expenses and time-specific scheduled expenses. Follow these rules and provide accurate, modifiable recommendations:\n\n" +
                "1) Recurring Expenses (e.g., meals):\n   - Use the \"식비\" category to calculate the daily meal budget by dividing the remaining budget for the category by the number of remaining days in the month.\n   - Assume three meals per day and calculate the per-meal budget.\n\n" +
                "2) Scheduled Expenses:\n   " +
                " **Categorization of Events**: For each remaining event this month, categorize them based on the event's name and time. Refer to the user's provided tomorrow_schedules and other_schedules_in_this_month data. Each category has events with descriptions and dates, and you need to apply the following rules" +
                "Categorize events based on their names to assign them to one of the following categories using natural language understanding:\n      - (1. 경조/선물 2. 교육/학습 3. 교통 4. 금융 5. 문화/여가 6. 반려동물 7. 뷰티/미용 \n        8. 생활 9. 술/유흥 10. 식비 11. 여행/숙박 12. 온라인 쇼핑 13. 의료/건강 \n        14. 자녀/육아 15. 자동차 16. 주거/통신 17. 카페/간식 18. 패션/쇼핑 \n        19. HANG-OUT 20. 기타)\n      - Assign a category only if it is explicitly clear from the event name. If there is ambiguity or difficulty in deciding, assign category 20 (ETC). For friend names or casual events or just random places, use category 19 (HANG-OUT). Avoid making assumptions about unclear schedules.\n   2. Calculate the budget for each event:\n      1) If the event belongs to a specific category, \n\t      - divide the remaining budget of that category by the number of remaining schedules in it.\n      2)If the event is categorized as \"19. HANG-OUT\" or \"20. 기타,\" \n\t      - Divide the total remaining budget across all categories by the total number of meaningful events remaining this month, irrespective of their categories. To determine how many \"meaningful\" events there are, YOU analyze all future events.\n\t\t\t\t- Identify meaningful events by analyzing the event's name and time to determine the likelihood of incurring costs. \n\t\t\t  - Look for keywords in the event name such as \"저녁\", \"카페\", \"결제\", \"쇼핑\", \"이벤트\".\n\t\t\t\tInstruction for Analysis:\n\t\t\t\tFor all remaining events this month:\n\t\t\t\t- Analyze the event name and time data.\n\t\t\t\t- Match keywords indicating potential spending, such as \"저녁\", \"카페\", \"결제\".\n\t\t\t\t- Mark events as meaningful if they match any keyword or fall within relevant times.\n   3. Retrieve external price suggestions as \"average price for this event among people in their 20s\" and present them to the user.\n   4. For each event, combine the event-specific budget and the external price suggestion. User the weight which the user provided to adjust the weight (default: 50-50 mix) for the budget calculation.\n\n" +
                "3) Total Daily Budget:\n   - Combine recurring and event-specific expenses to estimate the total daily budget.\n\nAdditional Requirements:\n   - Use the provided date and input data to ensure accurate budget calculations.\n   - Ensure all recommendations are easy for the user to modify after initial calculation.\n   \nResponse format: JSON - YOU SHOULD ONLY GIVE IN THIS FORMAT\n" +
                "example is like the following:  \"{\n" +
                "  \\\"recurring_expenses\\\": [\n" +
                "    {\n" +
                "      \\\"category\\\": \\\"string\\\",  // Category of the recurring expense (e.g., food, utilities, etc.)\n" +
                "      \\\"today_amount\\\": integer  // The amount for today's recurring expense\n" +
                "    }\n" +
                "  ],\n" +
                "  \"scheduled_expenses\\\": [\n" +
                "    {\n" +
                "      \\\"schedule_id\\\": Long, // schedule_id from the tomorrow_schedules (nullable) " +
                "      \\\"time\\\": \\\"string\\\",  // The scheduled date and time of the expense (e.g., '2024-11-12 07:16:02')\n" +
                "      \\\"category\\\": \\\"string\\\",  // Expected Category\n" +
                "      \\\"description\\\": \\\"string\\\",  // same as description from the input tomorrow_schedules. YOU SHOULD NOT CHANGE IT." +
                "      \\\"amount\\\": integer\n" +
                "    }\n" +
                "  ],\n\n" +
                "  \"daily_budget_total\": integer // sum of per_day_amount from the recurring_expenses list and the weighted_budget from the scheduled_expenses list" +
                "}\"\n";

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

        String userPromptTemplate =
                "I am providing budget data for the assistant to calculate my daily budget for the remaining days of this month. " +
                        "My goal is to divide expenses between recurring categories (like meals) and specific scheduled events. " +
                        "Here’s a detailed breakdown:\n" +
                        "Recurring Expense: {recurringCategory}\n" +
                        "The \"{recurringCategory}\" category has a remaining budget of {remainingBudget} and there are {remainingDays} days left in the month. " +
                        "Please divide this budget evenly across the remaining days and calculate a per-meal amount, assuming three meals per day.\n" +
                        "Scheduled Expenses\n" +
                        "In addition to recurring expenses, there are specific tomorrow_schedules for which I need an expense estimate. " +
                        "Here's how to handle these:\n" +
                        "Event Categorization: Please categorize the event \"{eventDescription}\" appropriately if possible. " +
                        "Otherwise, provide an option for me to choose later.\n" +
                        "Budget Allocation: The budget amount for tomorrow_schedules is especially important. Each event should receive a budget by dividing the remaining budget of its category by the number of remaining events in that category. " +
                        "Example: \"{eventCategory1}\" has a remaining budget of {remainingBudget1} and there are {remainingEvents1} events in this category. " +
                        "\"{eventCategory2}\" has a remaining budget of {remainingBudget2} and there are {remainingEvents2} events in this category.\n" +
                        "Price Suggestions: Include external price suggestions (e.g., average costs for similar activities among people in their 20s) if available. " +
                        "Combine the calculated category-based budget with the external price suggestion using the specified weight. The default weight is 0.5 for each unless I specify otherwise.";

        LocalDate lastDayOfMonth = tomorrowDate.with(TemporalAdjusters.lastDayOfMonth());  // Last day of the current month
        long remainingDays = tomorrowDate.until(lastDayOfMonth).getDays() + 1;  // Include today in the remaining days


        userContentObject1.put("text", userPromptTemplate);
        userContent1.put(userContentObject1);
        userMessage1.put("content", userContent1);

        // User Role 메시지 2 (Budget Data)
        JSONObject userMessage2 = new JSONObject();
        userMessage2.put("role", "user");

        JSONArray userContent2 = new JSONArray();
        JSONObject userContentObject2 = new JSONObject();

        // TODO: 12월에 날짜 바꾸기
        String jsonString = "{\n" +
                "  \"weight_for_category\": %.1f\n" +
                "  \"tomorrow_date\": \"%s\",\n" +  // 내일 날짜
                "  \"total_budget_amount\": %s,\n" +
                "  \"tomorrow_schedules\": %s,\n" +  // 내일의 소비일정
                "  \"category_budgets\": {\n" +
                "    \"교육/학습\": \"50000\",\n" +
                "    \"교통\": \"70000\",\n" +
                "    \"금융\": \"30000\",\n" +
                "    \"문화/여가\": \"30000\",\n" +
                "    \"뷰티/미용\": \"10000\",\n" +
                "    \"생활\": \"100000\",\n" +
                "    \"식비\": \"300000\",\n" +
                "    \"의료/건강\": \"20000\",\n" +
                "    \"카페/간식\": \"10000\",\n" +
                "    \"패션/쇼핑\": \"60000\"\n" +
                "  } " +
                "  \"other_schedules_in_this_month\": {\n" +
                "    \"문화/여가\": {\n" +
                "      \"남은 일정 개수\": 1,\n" +
                "      \"일정 목록\": [\n" +
                "        {\n" +
                "          \"description\": \"주말에 뮤지컬 관람\",\n" +
                "          \"time\": \"2024-12-30 11:16:02\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"교육/학습\": {\n" +
                "      \"남은 일정 개수\": 1,\n" +
                "      \"일정 목록\": [\n" +
                "        {\n" +
                "          \"description\": \"해커스 토플 인강 결제하기!\",\n" +
                "          \"time\": \"2024-12-30 10:00:00\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"카페/간식\": {\n" +
                "      \"남은 일정 개수\": 1,\n" +
                "      \"일정 목록\": [\n" +
                "        {\n" +
                "          \"description\": \"지민재현이랑 익선동 카페\",\n" +
                "          \"time\": \"2024-12-30 20:00:00\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String dataString = String.format(jsonString, weightForCategory, tomorrowDate, totalBudget.toString(), new JSONArray(scheduledExpenses));

        userContentObject2.put("type", "text");
        userContentObject2.put("text", "Provided Data: \n" + dataString);

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
        parameters.put("required", new JSONArray(List.of("recurring_expenses", "scheduled_expenses", "daily_budget_total")));

        JSONObject recurringExpenseSchema = new JSONObject();
        recurringExpenseSchema.put("type", "array");
        recurringExpenseSchema.put("items", new JSONObject()
                .put("type", "object")
                .put("required", new JSONArray(List.of("category", "remaining_budget", "remaining_days")))
                .put("properties", new JSONObject()
                        .put("category", new JSONObject().put("type", "string"))
                        .put("per_day_amount", new JSONObject().put("type", "integer"))
                        .put("remaining_days", new JSONObject().put("type", "integer"))
                        .put("per_meal_amount", new JSONObject().put("type", "integer"))
                        .put("remaining_budget", new JSONObject().put("type", "integer"))));

        JSONObject scheduledExpensesSchema = new JSONObject();
        scheduledExpensesSchema.put("type", "array");
        scheduledExpensesSchema.put("items", new JSONObject()
                .put("type", "object")
                .put("required", new JSONArray(List.of("time", "description", "category")))
                .put("properties", new JSONObject()
                        .put("time", new JSONObject().put("type", "string").put("format", "date-time"))
                        .put("category", new JSONObject().put("type", "string"))
                        .put("description", new JSONObject().put("type", "string"))
                        .put("category_budget", new JSONObject().put("type", "integer"))
                        .put("weighted_budget", new JSONObject().put("type", "integer"))
                        .put("per_event_budget", new JSONObject().put("type", "integer"))
                        .put("average_price_suggestion", new JSONObject().put("type", "integer"))
                        .put("remaining_events_in_category", new JSONObject().put("type", "integer"))));

        // parameters에 "required" 및 "properties" 추가
        parameters.put("type", "object");
        parameters.put("required", new JSONArray(List.of("recurring_expenses", "scheduled_expenses", "daily_budget_total")));
        parameters.put("properties", new JSONObject()
                .put("recurring_expenses", recurringExpenseSchema)
                .put("scheduled_expenses", scheduledExpensesSchema)
                .put("daily_budget_total", new JSONObject().put("type", "integer")));

        // functionDetails에 parameters 포함
        functionDetails.put("parameters", parameters);

        // tools 배열 구성
        JSONArray tools = new JSONArray();
        tools.put(new JSONObject().put("type", "function").put("function", functionDetails));

        // messages 배열에 response_format 및 tools 추가
        messages.put(new JSONObject().put("response_format", responseFormat));
        messages.put(new JSONObject().put("tools", tools));

        // OpenAI 서비스 호출 및 결과 반환
        String result = openAIService.generateGPTResponse(messages, 0.7, 2048, 1, 0, 0);

        // 정규식으로 백틱 안의 JSON만 추출
        String jsonContent = result.replaceAll("(?s).*```json(.*?)```.*", "$1").trim();
        // 백틱이 없을 경우에는 원본 결과 그대로 사용
        if (jsonContent.isEmpty()) {
            jsonContent = result.trim();
        }
        log.info("jsonContent:\n" + jsonContent);
        return jsonContent;
    }

    /**
     * 기능 2 - 월별 리포트에서 피드백 메시지를 생성하기 위한 메시지를 만듭니다.
     * @param totalBudgetAmount 전체 예산
     * @param categoryBudgets 카테고리별 예산 (Map 형식)
     * @param totalExpenseAmount 전체 지출
     * @param categoryExpenses 카테고리별 지출 (Map 형식)
     * @return 생성된 피드백 메시지 내용이 담긴 String
     */
    public String buildMonthlyReportMessage(
            BigDecimal totalBudgetAmount,
            Map<String, BigDecimal> categoryBudgets,
            BigDecimal totalExpenseAmount,
            Map<String, BigDecimal> categoryExpenses
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        final String GPT_PROMPT =  "You are a financial assistant designed to analyze a user's monthly budget and expenses. Your task is to compare their planned budget with actual expenses, identify areas of success and improvement, and provide actionable, motivational feedback for the next month. Follow these rules:\\n\\n1) Start with positive feedback on total and move in to each categories where the user stayed within or under budget.\\n 2) Identify categories where the user exceeded their budget, explaining why it might have happened.\\n   - Example: \\\"Transportation costs were higher than planned, likely due to unexpected trips.\\\"\\n\\n3) Provide actionable suggestions to improve spending habits next month.\\n   - Example: \\\"Consider reducing dining out and reallocating the savings to other categories.\\\"\\n\\n4) Use friendly and motivational language to encourage the user.\\n\\n5) Output the response as a single JSON object under the key \\\"response\\\".6) You should only have to give feedback in Korean, not English. 7) You will end up giving one paragaph(3~4 sentences in Korean) of feedback which should include everything, not separtely. That's the only thing you should give it. TOTAL SUMMARY OF FEEDBACK OF THIS MONTH. 8) put 2 emojis in the middle of sentence and say in friendly tone. You should focus on more positive things and emphasis more that negative things. The example of expected response is like '이번 달 총 예산을 90% 사용하시면서 예산 내에 소비를 성공하셨네요!! 특히 식비를 정말 잘 절약하셨어요! \uD83C\uDF89 하지만 외식비가 예산을 살짝 초과한 점이 아쉬워요. 다음 달에는 외식 횟수를 조금 줄이고 대신 식비 예산을 살짝 늘려서 균형을 맞춰보는 건 어떨까요? 이렇게 하면 더 많은 저축도 가능할 거예요! \uD83D\uDE0A'";
        
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

        // OpenAI 서비스 호출 및 응답 parsing
        String result = openAIService.generateGPTResponse(messages, 1.1, 200, 1, 0, 0);
        JSONObject jsonResponse = new JSONObject(result);
        String feedbackMessage = jsonResponse.getString("response");

        // 이스케이프 처리
        feedbackMessage = feedbackMessage.replace("\\n", "\n").replace("\\", "");
        return feedbackMessage;
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
     * @param userRequestedBudget      다음 달 전체 예산 (총 금액)
     * @return OpenAI GPT 모델에 전달할 JSON 메시지
     */
    public String buildMonthlyBudgetMessage(
            Map<String, Integer> currentBudget,
            String feedbackMessage,
            List<Map<String, Object>> fixedExpenses,
            String userOpinions,
            List<Map<String, Object>> nextMonthSchedules,
            BigDecimal userRequestedBudget
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        String GPT_PROMPT =
                "You are a financial assistant AI responsible for creating a personalized monthly budget. Analyze the provided data and generate a budget plan for the next month in JSON format. Ensure the budget is tailored for 30 days and within the fixed total budget. Assign reasonable amounts to categories, focusing on essential costs like food and living expenses. Explain the reasoning behind your allocations in the reason field in Korean.\n" +
                        "\n" +
                        "Inputs:\n" +
                        "\n" +
                        "Previous month's budget and spending feedback\n" +
                        "Fixed expenses (non-negotiable)\n" +
                        "User’s preferences and next month's plans (e.g., special events, travel)\n" +
                        "Total budget for next month (fixed total budget)\n" +
                        "### Key Constraints:\n" +
                        "- After distributing the `total_budget` across categories, ensure that the sum of category budgets matches the `total_budget` exactly. If necessary, adjust the allocations proportionally.\n" +
                        "- In the explanation (`reason` field), integrate the actual category names from the user's data into the narrative where appropriate. Avoid mentioning the exact total budget in the explanation. Focus on explaining adjustments and rationale for each category based on user feedback and past spending patterns.\n" +
                        "### Output Requirements:\nGenerate the budget in the following JSON format:\n" +
                        "{\n  \"monthly_budget\": {\n    \"total_budget\": {0},\n    \"category_budgets\": {\n      \"경조/선물\": 0,\n      \"교육/학습\": 0,\n      \"교통\": 0,\n      \"금융\": 0,\n      \"문화/여가\": 0,\n      \"반려동물\": 0,\n      \"뷰티/미용\": 0,\n      \"생활\": 0,\n      \"술/유흥\": 0,\n      \"식비\": 0,\n      \"여행/숙박\": 0,\n      \"온라인 쇼핑\": 0,\n      \"의료/건강\": 0,\n      \"자녀/육아\": 0,\n      \"자동차\": 0,\n      \"주거/통신\": 0,\n      \"카페/간식\": 0,\n      \"패션/쇼핑\": 0\n    }\n  },\n  " +
                        "  \"reason\": \"...\"}\n" +
                        "Guide for reason:\n" +
                        "You are a friendly financial assistant helping analyze a user's monthly budget and spending. Your goal is to provide a thoughtful, motivating explanation in Korean for budget adjustments based on user feedback and spending habits. Focus on positive aspects and gently suggest improvements where needed.\n" +
                        "\n" +
                        "For overspending categories, explain why it happened and suggest increasing the budget for the next month.\n" +
                        "For categories with underspending, recommend reducing the budget slightly.\n" +
                        "Offer actionable suggestions for balance and sustainability, keeping the response 8-12 sentences long.\n" +
                        "Avoid specific numerical values; focus on explaining category changes and the reasoning behind them.\n" +
                        "Keep the tone warm and encouraging while adapting your response to fit the user's situation.";

//                "You are a financial assistant AI responsible for creating a personalized monthly budget. Your task is to analyze the provided data and generate a budget plan for the next month in JSON format. Ensure the budget is tailored for 30 days (one month). Assign categories reasonably, especially for essential costs like food and living expenses, but within the fixed total budget. Explain the reasoning behind your allocations in detail in Korean in the `reason` field. You should assign a REASONABLE budget for 'Next Month's Plans' and each category. Your thinking process for planning next month budget should be like this. Consider Total budget and fixed expense for first(both are non-negotiable), and then get this month's budget and spending feedback for getting blue-map for the categories proportion, and then get User's preference and next month's plan to make the budget you gave will more fit to user themselves. This is the priority. Please keep non-negotiable things(total budget for next month and Fixed expense) \n\n" +
//                "### Inputs:\n1. **Previous Month's Budget**: The budget allocated to each category previous month. \n" +
//                "2. **Previous Month's Spending Feedback**: Feedback on whether the user overspent or underspent in various categories previous month.\n" +
//                "3. **Fixed Expenses**: Non-negotiable expenses such as rent, utilities, or loan repayments that must be covered.\n" +
//                "4. **User's Preferences**: User's specific preferences or priorities for the next month. \n   - If the user's preferences cannot be fully accommodated without exceeding the total budget or making other categories severely underfunded, partially adjust the allocations and explain the reasons clearly in the `reason` field. If you cannot make it, YOU SHOULD EXPLAIN IN THE RESON THO.\n" +
//                "5. **Next Month's Plans**: Include additional expenses for special events, holidays, or other significant activities. You should assign reasonable amount of cost. Especially if it's travel to somewhere, you should calculate all the flight and accomodations, and food, etc. \n" +
//                "6. **Total Budget for next month**: The Non-negotiable maximum budget available for allocation next month (e.g., {0} KRW) There is no way you can exceed or underallocate the budget in there. THIS IS FIXED TOTAL BUDGET. At last you should sum up all of categories budget and if it doesn't fit to total fixed budget, you should arrange by applying proportions for all the categories so you should end up fit to total budget. THERE IS NO NEGOTIATION FOR THIS.\n\n" +
//                "### Key Constraints:\n" +
//                "- After distributing the `total_budget` across categories, ensure that the sum of category budgets matches the `total_budget` exactly. If necessary, adjust the allocations proportionally.\n" +
//                "- In the explanation (`reason` field), integrate the actual category names from the user's data into the narrative where appropriate. Avoid mentioning the exact total budget in the explanation. Focus on explaining adjustments and rationale for each category based on user feedback and past spending patterns.\n" +
//                "### Output Requirements:\nGenerate the budget in the following JSON format:\n" +
//                "{\n  \"monthly_budget\": {\n    \"total_budget\": {0},\n    \"category_budgets\": {\n      \"경조/선물\": 0,\n      \"교육/학습\": 0,\n      \"교통\": 0,\n      \"금융\": 0,\n      \"문화/여가\": 0,\n      \"반려동물\": 0,\n      \"뷰티/미용\": 0,\n      \"생활\": 0,\n      \"술/유흥\": 0,\n      \"식비\": 0,\n      \"여행/숙박\": 0,\n      \"온라인 쇼핑\": 0,\n      \"의료/건강\": 0,\n      \"자녀/육아\": 0,\n      \"자동차\": 0,\n      \"주거/통신\": 0,\n      \"카페/간식\": 0,\n      \"패션/쇼핑\": 0\n    }\n  },\n  " +
//                "  \"reason\": \"...\"}\n" +
//                "guide about `reason`: You are a friendly financial assistant tasked with analyzing a user's monthly budget and spending habits. Your goal is to provide a thoughtful, concise, and motivating comment in Korean, explaining why certain categories have higher or lower budgets compared to previous months based on user feedback and spending patterns. Focus on positive aspects and offer gentle suggestions for improvements where necessary.\n\n" +
//                "Feel free to structure your response in any way that fits the user's situation. While explaining overspending in certain categories, suggest increasing the budget for those categories in the next month. Similarly, for categories where the user spent less than planned, suggest reducing the budget for those categories.\n\n" +
//                "Your response should be 8-12 sentences long. It should be motivating, with a focus on positive reinforcement, and gentle suggestions for areas to improve. Avoid mentioning specific numerical values or figures from the budget.\n\n" +
//                "Here are some guidelines to help you form your response:\n\n" +
//                "1) Focus on the positive aspects first – highlight areas where the user stayed within or under the budget.\n" +
//                "2) For overspending categories, explain why it happened and suggest increasing the budget for those categories in the next month.\n" +
//                "3) For categories where the user saved money or spent less than planned, suggest reducing the budget slightly.\n" +
//                "4) Offer actionable suggestions to improve spending habits for the next month, with a focus on balance and sustainability.\n" +
//                "5) Your response can follow a variety of structures. You can mix the categories based on importance or sequence, and adapt the tone to make it friendly and motivational.\n\n" +
//                "Remember, the response should be in Korean and feel warm and encouraging. Avoid listing specific figures, and replace the placeholders with actual category names based on the user's data.\n\n" +
//                "Example:\n\n" +
//                "'이번 달 예산은 사용자님의 생활 패턴과 피드백을 반영하여 짜 보았어요. 필수적인 고정비인 {EssentialCategory1}과 {EssentialCategory2}는 우선적으로 배정했어요. 지난달 {OverusedCategory}에서 초과 사용된 점을 고려하여 예산을 늘렸어요. 사용이 적었던 {UnderusedCategory}는 예산을 조금 줄였어요. 계획한 활동에 맞춰 {PlannedEventCategory}에는 추가 예산을 배정했어요. 다음 달에는 {ImprovementCategory}를 더 잘 관리할 수 있도록 함께 노력해요. 😊'\n\n" +
//                "Please adapt the structure to fit the user's unique situation and feedback.";
                //"You are a friendly financial assistant tasked with analyzing a user's monthly budget and spending habits. Your goal is to provide a thoughtful, concise, and motivating comment in Korean, explaining why certain categories have higher or lower budgets compared to previous months based on user feedback and spending patterns. Focus on positive aspects and offer gentle suggestions for improvements where necessary.\n\nFeel free to structure your response in any way that fits the user's situation. While explaining overspending in certain categories, consider whether it's better to increase or decrease the budget for those categories. If a category exceeded the budget, suggest how to handle this for the next month, either by reducing the spending or adjusting the budget in a balanced way.\n\nYour response should be 8-12 sentences long. It should be motivating, with a focus on positive reinforcement, and gentle suggestions for areas to improve. Avoid mentioning specific numerical values or figures from the budget.\n\nHere are some guidelines to help you form your response:\n\n1) Focus on the positive aspects first – highlight areas where the user stayed within or under the budget.\n2) For overspending categories, explain why it happened and suggest how to adjust for the future.\n3) Offer actionable suggestions to improve spending habits for the next month.\n4) Your response can follow a variety of structures. You can mix the categories based on importance or sequence, and adapt the tone to make it friendly and motivational.\n\nRemember, the response should be in Korean and feel warm and encouraging. Avoid listing specific figures, and replace the placeholders with actual category names based on the user's data.\n\nExample:\n\n'다음 달 예산은 사용자님의 생활 패턴과 피드백을 반영하여 짜 보았어요. 필수적인 고정비인 {EssentialCategory1}과 {EssentialCategory2}는 우선적으로 배정했어요. 이번 달 {OverusedCategory}에서 초과 사용된 점을 고려하여 예산을 조금 늘렸어요. 사용이 적었던 {UnderusedCategory}는 예산을 조정하여 더 균형 있게 배정했어요. 계획한 활동에 맞춰 {PlannedEventCategory}에는 추가 예산을 배정했어요. 다음 달에는 {ImprovementCategory}를 더 잘 관리할 수 있도록 함께 노력해요. 다음 달도 계획한 일정이 잘 이루어지기를 바라요.😊'\n\nPlease adapt the structure to fit the user's unique situation and feedback.";
                //"Generate a Korean explanation using category names from the user's data. Use the following template as a guide:\\n\\n'이번 달 예산은 사용자님의 생활 패턴과 피드백을 반영해서 짜 보았어요. 필수적으로 지출해야 하는 고정비 {EssentialCategory1}와 {EssentialCategory2}는 먼저 배정했어요. 지난달 {OverusedCategory}에서 초과 사용된 점을 고려해 금액을 조금 늘렸어요. 상대적으로 사용 빈도가 적었던 {UnderusedCategory} 항목은 금액을 줄였어요. 또한, 계획된 활동 {PlannedEventCategory}에 맞춰 새로 금액을 배정했어요. 요청한 항목 중 예산 부담이 큰 {HighDemandCategory}는 조정했지만 최대한 반영하려 노력했답니다. 다음 달도 계획한 일정이 잘 이루어지기를 바라요.\uD83D\uDE0A'\\n\\nReplace placeholders with actual category names that match the user's feedback and spending patterns.\"\n}";
        String formattedString = String.format(GPT_PROMPT, userRequestedBudget);

        // 메시지를 저장할 JSON 배열 생성
        JSONArray messages = new JSONArray();

        // 시스템 메시지 추가
        JSONObject systemMessage = createSystemMessage(formattedString);
        messages.put(systemMessage);

        // 사용자 데이터 입력 메시지 생성
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        JSONObject userData = new JSONObject();

        // 현재 예산
        userData.put("current_budget", new JSONObject()
                .put("monthly_budget", new JSONObject()
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

        // 다음 달 예산
        userData.put("total_budget", userRequestedBudget);

        // 응답 포맷 추가
        userData.put("response_format", new JSONObject()
                .put("type", "json_object"));

        // 사용자 메시지에 데이터 추가
        userMessage.put("content", userData);
        messages.put(userMessage);

        // OpenAI 서비스 호출 및 결과 반환
        String result = openAIService.generateGPTResponse(messages, 1.0, 2048, 1, 0, 0);

        // 정규식으로 백틱 안의 JSON만 추출
        String jsonContent = result.replaceAll("(?s).*```json(.*?)```.*", "$1").trim();
        // 백틱이 없을 경우에는 원본 결과 그대로 사용
        if (jsonContent.isEmpty()) {
            jsonContent = result.trim();
        }
        return jsonContent;
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

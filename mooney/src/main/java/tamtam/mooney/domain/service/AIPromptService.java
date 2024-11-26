package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import tamtam.mooney.global.openai.OpenAIService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIPromptService {
    private final OpenAIService openAIService;

    /**
     * 기능 2 - 월별 리포트에서 피드백 메시지를 생성하기 위한 메시지를 만듭니다.
     * @param totalBudget 전체 예산 (double 형식)
     * @param categoryBudgets 카테고리별 예산 (Map 형식)
     * @param monthlyExpenses 월간 지출 데이터 (List<Map<String, Object>> 형식)
     * @return 생성된 피드백 메시지 내용이 담긴 String
     */
    public String buildThisMonthReportMessages(
            double totalBudget,
            Map<String, Integer> categoryBudgets,
            List<Map<String, Object>> monthlyExpenses
    ) {
        // GPT 모델이 분석할 프롬프트 정의
        final String GPT_PROMPT = "You are a financial assistant designed to analyze a user's monthly budget and expenses. Your task is to compare their planned budget with actual expenses, identify areas of success and improvement, and provide actionable, motivational feedback for the next month. Follow these rules:\\n\\n1) Start with positive feedback on total and move in to each categories where the user stayed within or under budget.\\n 2) Identify categories where the user exceeded their budget, explaining why it might have happened.\\n   - Example: \\\"Transportation costs were higher than planned, likely due to unexpected trips.\\\"\\n\\n3) Provide actionable suggestions to improve spending habits next month.\\n   - Example: \\\"Consider reducing dining out and reallocating the savings to other categories.\\\"\\n\\n4) Use friendly and motivational language to encourage the user.\\n\\n5) Output the response as a single JSON object under the key \\\"response\\\".6) You should only have to give feedback in Korean, not English. 7) You will end up giving one paragaph(3~4 sentences in Korean) of feedback which should include everything, not separtely. That's the only thing you should give it. TOTAL SUMMARY OF FEEDBACK OF THIS MONTH. 8) put 2 emojis in the middle of sentence and say in friendly tone. You should focus on more positive things and emphasis more that negative things. The example of expected response is like '이번 달 총 예산을 90% 사용하시면서 예산 내에 소비를 성공하셨네요!! 특히 식비를 정말 잘 절약하셨어요! \uD83C\uDF89 하지만 외식비가 예산을 살짝 초과한 점이 아쉬워요. 다음 달에는 외식 횟수를 조금 줄이고 대신 식비 예산을 살짝 늘려서 균형을 맞춰보는 건 어떨까요? 이렇게 하면 더 많은 저축도 가능할 거예요! \uD83D\uDE0A'";

        // 메시지를 저장할 JSON 배열 생성
        JSONArray messages = new JSONArray();

        // system 메시지
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", GPT_PROMPT));

        // user 메시지
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", new JSONObject()
                        .put("monthly_expenses", new JSONArray(monthlyExpenses))
                        .put("total_budget", totalBudget)
                        .put("category_budgets", categoryBudgets)).toString());

        // OpenAI 서비스 호출 및 결과 반환
        return openAIService.generateGPTResponse(messages, 1.1, 200, 1, 0, 0);
    }
}

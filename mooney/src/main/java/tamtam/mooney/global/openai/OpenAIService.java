package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
@RequiredArgsConstructor
public class OpenAIService {
    private final OpenAiChatModel openAiChatModel;
    private final OpenAIConfig openAIConfig;

    public String generateGPTResponse(JSONArray messages, double temperature, int max_tokens, double top_p, double frequency_penalty, double presence_penalty) {
        String requestBody = buildOpenAIRequestBody(messages, temperature, max_tokens, top_p, frequency_penalty, presence_penalty);
        // OpenAI Chat 모델 호출 및 응답 반환
        return openAiChatModel.call(requestBody);
    }

    // OpenAI API에 전달할 요청 본문을 생성하는 메서드
    private String buildOpenAIRequestBody(JSONArray messages, double temperature, int max_tokens, double top_p, double frequency_penalty, double presence_penalty
    ) {
        // JSON 구조 생성
        JSONObject requestBody = new JSONObject();

        // OpenAI 모델 설정
        requestBody.put("model", openAIConfig.getModel());

        // messages 배열을 요청 본문에 추가
        requestBody.put("messages", messages);

        // 추가 속성 설정
        requestBody.put("temperature", temperature); // 응답의 무작위성 조절
        requestBody.put("max_tokens", max_tokens); // 응답의 최대 토큰 수 설정
        requestBody.put("top_p", top_p); // 확률 기반 샘플링 설정
        requestBody.put("frequency_penalty", frequency_penalty); // 반복 단어에 대한 페널티 설정
        requestBody.put("presence_penalty", presence_penalty); // 새로운 단어 출현에 대한 페널티 설정

        // JSON 문자열 반환 (2칸 들여쓰기)
        return requestBody.toString(2);
    }
}

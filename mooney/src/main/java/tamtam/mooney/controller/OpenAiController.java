package tamtam.mooney.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tamtam.mooney.service.aiService.GPTService;

@RestController
@RequiredArgsConstructor
public class OpenAiController {
    private final GPTService GPTService;

    @GetMapping("/test-openai")
    public String testOpenAiConnection() {
        try {
            GPTService.testOpenAiConnection();
            return "OpenAI API 테스트 요청이 성공적으로 전송되었습니다. 결과는 콘솔을 확인하세요.";
        } catch (Exception e) {
            e.printStackTrace();
            return "OpenAI API 요청 중 오류가 발생했습니다.";
        }
    }
}


package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ChatController {
    private final TestInputService testInputService;

    @GetMapping("/ai")
    public String chat() {
        return testInputService.generateThisMonthReport();
    }
}
package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ChatController {
    private final TestInputService testInputService;

    @GetMapping("/ai/tomorrow-budget")
    public String tomorrowBudget() {
        return testInputService.testBuildDailyBudget();
    }

    @GetMapping("/ai/this-month-report")
    public String monthlyReport() {
        return testInputService.testBuildMonthlyReport();
    }

    @GetMapping("/ai/next-month-budget")
    public String monthlyBudget(String userMessage) {
        return testInputService.testBuildMonthlyBudget(userMessage);
    }
}
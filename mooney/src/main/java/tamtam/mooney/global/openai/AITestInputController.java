package tamtam.mooney.global.openai;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.global.openai.dto.DailyBudgetInputRequestDto;
import tamtam.mooney.global.openai.dto.MonthlyBudgetInputRequestDto;
import tamtam.mooney.global.openai.dto.MonthlyReportInputRequestDto;

@Tag(name = "OpenAITestInput")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AITestInputController {
    private final AiTestInputService aiTestInputService;

    @PostMapping("/tomorrow-budget")
    public String tomorrowBudget(@RequestBody DailyBudgetInputRequestDto requestDto) {
        return aiTestInputService.buildDailyBudgetWithRequestBody(requestDto);
    }

    @PostMapping("/this-month-report")
    public String monthlyReport(@RequestBody MonthlyReportInputRequestDto requestDto) {
        return aiTestInputService.buildMonthlyReportWithRequestBody(requestDto);
    }

    @PostMapping("/next-month-budget")
    public String monthlyBudget(@RequestBody MonthlyBudgetInputRequestDto requestDto) {
        return aiTestInputService.buildMonthlyBudgetWithRequestBody(requestDto);
    }
}
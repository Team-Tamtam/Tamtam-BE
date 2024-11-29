package tamtam.mooney.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.dto.MergeSchedulesRequest;
import tamtam.mooney.domain.dto.request.DailyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.DailyBudgetResponseDto;
import tamtam.mooney.domain.dto.request.MonthlyBudgetRequestDto;
import tamtam.mooney.domain.dto.response.MonthlyBudgetResponseDto;
import tamtam.mooney.domain.service.DailyBudgetService;
import tamtam.mooney.domain.service.MonthlyBudgetService;

@Slf4j
@Tag(name = "Budget")
@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetContoller {
    private final DailyBudgetService dailyBudgetService;
    private final MonthlyBudgetService monthlyBudgetService;

    @Operation(summary = "내일 소비 예정 일정 선택 & GPT가 세운 내일 예산 가져오기")
    @PostMapping("/tomorrow")
    public ResponseEntity<?> createTomorrowBudgetAndSchedules(@RequestBody @Valid DailyBudgetRequestDto requestDto) {
        DailyBudgetResponseDto responseDto = dailyBudgetService.getTomorrowBudgetAndSchedules(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "GPT에게 다음 달 예산 계획 요구사항 전달 & 응답으로 예산 및 주요 일정 받기")
    @PostMapping("/next-month")
    public ResponseEntity<?> createNextMonthBudget(@RequestBody @Valid MonthlyBudgetRequestDto requestDto) {
        MonthlyBudgetResponseDto responseDto = monthlyBudgetService.createNextMonthBudget(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/merge-schedules")
    public DailyBudgetResponseDto mergeSchedulesWithJsonData(
            @RequestBody MergeSchedulesRequest request
    ) {
        log.info("Received mergeSchedulesWithJsonData request: {}", request);

        try {
            DailyBudgetResponseDto response = dailyBudgetService.mergeSchedulesWithJsonData(
                    request.getRepeatedSchedules(),
                    request.getTomorrowSchedules(),
                    request.getJsonResponse()
            );
            log.info("Successfully merged schedules. Budget amount: {}", response.getBudgetAmount());
            return response;
        } catch (Exception e) {
            log.error("Error merging schedules with JSON data", e);
            throw new RuntimeException("Error processing the request", e);
        }
    }
}

package tamtam.mooney.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.service.CategoryExpenseService;
import tamtam.mooney.domain.service.MonthlyReportService;

@Tag(name = "Report")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final MonthlyReportService monthlyReportService;
    private final CategoryExpenseService categoryExpenseService;

    @Operation(summary = "월 소비 피드백 가져오기")
    @PostMapping("/monthly")
    public ResponseEntity<?> getMonthlyExpenseReport(@RequestParam final int year,
                                                     @RequestParam final int month) {
        return ResponseEntity.ok(monthlyReportService.getMonthlyReport(year, month));
    }

    @Operation(summary = "월 소비 카테고리 별 퍼센트, 지난 달 반영 예산값 가져오기")
    @PostMapping("/categories")
    public ResponseEntity<?> getCategoryExpenseStats(@RequestParam final int year,
                                                     @RequestParam final int month) {
        return ResponseEntity.ok(categoryExpenseService.getCategoryExpenseStats(year, month));
    }
}

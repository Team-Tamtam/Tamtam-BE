package tamtam.mooney.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.dto.ScheduleDto;
import tamtam.mooney.service.ScheduleService;

import java.util.List;

@Tag(name = "Schedule")
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @Operation(summary = "내일 일정 데이터 가져오기")
    @GetMapping("/tomorrow")
    public ResponseEntity<?> getSchedulesForTomorrow() {
        List<ScheduleDto> schedules = scheduleService.getOrInsertSchedulesForTomorrow();
        return ResponseEntity.ok(schedules);
    }
}

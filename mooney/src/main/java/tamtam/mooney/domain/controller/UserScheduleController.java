package tamtam.mooney.domain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.dto.common.UserScheduleDto;
import tamtam.mooney.domain.service.UserScheduleService;

import java.util.List;

@Tag(name = "UserSchedule")
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class UserScheduleController {
    private final UserScheduleService userScheduleService;

    @Operation(summary = "내일 일정 데이터 가져오기")
    @GetMapping("/tomorrow")
    public ResponseEntity<?> getSchedulesForTomorrow() {
        List<UserScheduleDto> schedules = userScheduleService.getSchedulesForTomorrow();
        return ResponseEntity.ok(schedules);
    }
}

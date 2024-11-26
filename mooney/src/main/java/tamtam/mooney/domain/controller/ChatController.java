package tamtam.mooney.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tamtam.mooney.domain.service.TestInputService;

@RequiredArgsConstructor
@RestController
public class ChatController {
    private final TestInputService testInputService;

    @GetMapping("/ai")
    public String chat() {
        return testInputService.generateThisMonthReport();
    }
}
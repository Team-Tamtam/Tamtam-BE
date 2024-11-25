package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.MonthlyBudgetRequestDto;
import tamtam.mooney.dto.MonthlyBudgetResponseDto;
import tamtam.mooney.dto.GptMonthlyBudgetResponseDto;
import tamtam.mooney.entity.MonthlyBudget;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.MonthlyBudgetRepository;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final UserService userService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    @Transactional
    public MonthlyBudgetResponseDto createNextMonthBudget(MonthlyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // GPT로부터 데이터 받기
        GptMonthlyBudgetResponseDto gptResponse = null; // planNextMonthBudget(requestDto,...);


        // GPT 응답 데이터를 MonthlyBudget에 저장
        MonthlyBudget monthlyBudget = MonthlyBudget.builder()
                .period(getNextMonthPeriod()) // 다음 달 기간 (예: "2024-12")
                .initialAmount(gptResponse.getTotalBudget())
                .finalAmount(gptResponse.getTotalBudget()) // 초기 값과 동일
                .user(user)
                .build();

        monthlyBudgetRepository.save(monthlyBudget); // 저장

        // 응답 데이터 변환
        return MonthlyBudgetResponseDto.builder()
                .budgetAmount(gptResponse.getTotalBudget())
                .categories(
                        gptResponse.getCategoryBudgets().stream()
                                .map(category -> MonthlyBudgetResponseDto.CategoryBudgetDto.builder()
                                        .categoryId(category.getId())
                                        .categoryName(category.getCategoryName())
                                        .categoryBudgetAmount(category.getBudgetAmount())
                                        .build()
                                ).collect(Collectors.toList())
                )
                .keySchedules(
                        gptResponse.getSchedules().stream()
                                .map(schedule -> MonthlyBudgetResponseDto.ScheduleDto.builder()
                                        .scheduleId(schedule.getId())
                                        .title(schedule.getTitle())
                                        .categoryName(schedule.getCategoryName())
                                        .startDateTime(schedule.getStartTime())
                                        .endDateTime(schedule.getEndTime())
                                        .build()
                                ).collect(Collectors.toList())
                )
                .build();
    }

    // 다음 달의 기간 반환
    private String getNextMonthPeriod() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMonth = now.plusMonths(1);
        return nextMonth.getYear() + "-" + String.format("%02d", nextMonth.getMonthValue());
    }
}

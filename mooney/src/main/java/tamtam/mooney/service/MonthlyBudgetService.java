package tamtam.mooney.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.dto.MonthlyBudgetRequestDto;
import tamtam.mooney.dto.MonthlyBudgetResponseDto;
import tamtam.mooney.dto.GptMonthlyBudgetResponseDto;
import tamtam.mooney.entity.MonthlyBudget;
import tamtam.mooney.entity.User;
import tamtam.mooney.repository.MonthlyBudgetRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final UserService userService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    public MonthlyBudgetResponseDto createNextMonthBudget(MonthlyBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // GPT로부터 데이터 받기 (테스트 데이터)
        GptMonthlyBudgetResponseDto gptResponse = GptMonthlyBudgetResponseDto.builder()
                .totalBudget(requestDto.budgetAmount())
                .categoryBudgets(Arrays.asList(
                        new GptMonthlyBudgetResponseDto.GptCategoryDto(1L, "FOOD", new BigDecimal("350000")),
                        new GptMonthlyBudgetResponseDto.GptCategoryDto(2L, "TRANSPORT", new BigDecimal("120000"))
                ))
                .schedules(Arrays.asList(
                        new GptMonthlyBudgetResponseDto.GptScheduleDto(301L, "Business Trip", "TRANSPORT", "2024-12-05T09:00:00", "2024-12-08T18:00:00"),
                        new GptMonthlyBudgetResponseDto.GptScheduleDto(302L, "Family Dinner", "FOOD", "2024-12-20T19:00:00", "2024-12-20T21:00:00")
                ))
                .build();

        // GPT 응답 데이터를 MonthlyBudget에 저장
        MonthlyBudget monthlyBudget = MonthlyBudget.builder()
                .period(getNextMonthPeriod()) // 다음 달 기간 (예: "2024-12")
                .initialAmount(gptResponse.getTotalBudget())
                .finalAmount(gptResponse.getTotalBudget())
                .user(user)
                .build();

        monthlyBudgetRepository.save(monthlyBudget);

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

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyBudgetAmount(User user, String period) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByUserAndPeriod(user, period)
                .orElseThrow(() -> new IllegalArgumentException("해당 월에 대한 예산 정보가 없습니다."));
        return monthlyBudget.getFinalAmount() != null ? monthlyBudget.getFinalAmount() : monthlyBudget.getInitialAmount();
    }

    // 테스트 데이터
    @PostConstruct
    public void initMonthlyBudget() {
        User currentUser = userService.getCurrentUser();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 예산이 없는 경우 기본 예산을 설정
        if (monthlyBudgetRepository.findByUserAndPeriod(currentUser, currentMonth).isEmpty()) {
            MonthlyBudget defaultBudget = MonthlyBudget.builder()
                    .period(currentMonth)
                    .initialAmount(BigDecimal.valueOf(850000))
                    .finalAmount(BigDecimal.valueOf(850000))
                    .user(currentUser)
                    .build();
            monthlyBudgetRepository.save(defaultBudget); // 예산 저장
        }
    }
}

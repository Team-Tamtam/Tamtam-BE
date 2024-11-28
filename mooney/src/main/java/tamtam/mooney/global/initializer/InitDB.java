package tamtam.mooney.global.initializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tamtam.mooney.domain.entity.*;
import tamtam.mooney.domain.repository.*;
import tamtam.mooney.domain.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InitDB {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final UserRepository userRepository;
    private final UserScheduleRepository userScheduleRepository;
    private final UserService userService;

    private final String testAccountEmail = "test@domain.com";

    @PostConstruct
    public void createTestAccount() {
        if (userRepository.findById(1L).isEmpty()) {
            User testAccount = User.builder()
                    .userId(1L)
                    .email(testAccountEmail)
                    .loginType("GOOGLE")
                    .nickname("화연")
                    .build();
            userRepository.save(testAccount);
        }
    }

    @PostConstruct
    public void initCategory() {
        if (categoryRepository.count() == 0) {
            for (CategoryName categoryName : CategoryName.values()) {
                categoryRepository.save(
                        Category.builder()
                                .categoryName(categoryName)
                                .icon(categoryName.getIcon())
                                .build()
                );
            }
        }
    }

    @PostConstruct
    public void initExpense() {
        User user = userService.getCurrentUser();
        // 값이 있으면 초기화 작업을 하지 않음
        if (expenseRepository.count() > 0) {
            return;
        }

        // `Expense` 데이터 삽입
        Expense expense1 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(1))
                .amount(BigDecimal.valueOf(5000))
                .description("식사")
                .user(user)
                .paymentMethod("신용카드")
                .categoryName(CategoryName.FOOD)
                .build();

        Expense expense2 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(2))
                .amount(BigDecimal.valueOf(10000))
                .description("교통비")
                .user(user)
                .paymentMethod("체크카드")
                .categoryName(CategoryName.TRANSPORT)
                .build();

        Expense expense3 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(3))
                .amount(BigDecimal.valueOf(5000))
                .description("학습 자료 구입")
                .user(user)
                .paymentMethod("카카오페이")
                .categoryName(CategoryName.EDUCATION)
                .build();

        Expense expense4 = Expense.builder()
                .transactionDateTime(LocalDateTime.now().minusDays(5))
                .amount(BigDecimal.valueOf(2000))
                .description("영화 관람")
                .user(user)
                .paymentMethod("현금")
                .categoryName(CategoryName.ENTERTAINMENT)
                .build();

        expenseRepository.save(expense1);
        expenseRepository.save(expense2);
        expenseRepository.save(expense3);
        expenseRepository.save(expense4);
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

    @PostConstruct
    public void initSchedules() {
        User user = userService.getCurrentUser();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        if (userScheduleRepository.findByUserAndStartDateTimeBetween(user, startOfTomorrow, startOfTomorrow.plusDays(1)).isEmpty()) {
            createAndSaveDefaultSchedules(user, startOfTomorrow);
        }
    }

    // This method will create and save the default schedules
    private void createAndSaveDefaultSchedules(User user, LocalDateTime startOfTomorrow) {
        List<UserSchedule> defaultSchedules = new ArrayList<>();

        defaultSchedules.add(UserSchedule.builder()
                .title("채플 시작")
                .startDateTime(startOfTomorrow.plusHours(10))
                .endDateTime(startOfTomorrow.plusHours(11))
                .location("이화여자대학교 대강당")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());

        defaultSchedules.add(UserSchedule.builder()
                .title("3시반 미술 전시회")
                .startDateTime(startOfTomorrow.plusHours(14))
                .endDateTime(startOfTomorrow.plusHours(16))
                .location("그라운드시소 서촌")
                .user(user)
                .build());

        defaultSchedules.add(UserSchedule.builder()
                .title("바른학원")
                .startDateTime(startOfTomorrow.plusHours(18))
                .endDateTime(startOfTomorrow.plusHours(19))
                .location("바른학원")
                .isRepeating(true)
                .repeatType("WEEKLY")
                .user(user)
                .build());

        // Save all default schedules
        userScheduleRepository.saveAll(defaultSchedules);
    }
}

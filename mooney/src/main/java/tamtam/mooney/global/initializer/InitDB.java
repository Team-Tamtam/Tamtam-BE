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
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1); // 첫 번째 날짜 (현재 월의 첫 날)
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth()); // 마지막 날짜 (현재 월의 마지막 날)

        // 현재 월의 총 지출이 0이 아니면 초기화 작업을 하지 않음
        BigDecimal totalExpenseThisMonth = expenseRepository.findTotalExpenseAmountByUserAndMonth(user, startOfMonth, endOfMonth);
        if (totalExpenseThisMonth.compareTo(BigDecimal.ZERO) != 0) {
            return; // 현재 월에 지출이 있으면 초기화 작업을 하지 않음
        }

        // JSON 데이터를 Expense 객체로 변환하여 삽입
        List<Expense> expenses = List.of(
                new Expense(LocalDateTime.of(2024, 11, 1, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "신용카드", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 1, 0, 0), BigDecimal.valueOf(2500), "지하철 이용", user, "체크카드", CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 2, 0, 0), BigDecimal.valueOf(8000), "커피와 디저트", user, "카카오페이", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 2, 0, 0), BigDecimal.valueOf(12000), "영화 관람", user, "현금", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 3, 0, 0), BigDecimal.valueOf(35000), "의류 구매", user, "신용카드", CategoryName.FASHION),
                new Expense(LocalDateTime.of(2024, 11, 4, 0, 0), BigDecimal.valueOf(20000), "저녁 외식", user, "체크카드", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 5, 0, 0), BigDecimal.valueOf(15000), "헤어 컷", user, "카카오페이", CategoryName.BEAUTY),
                new Expense(LocalDateTime.of(2024, 11, 6, 0, 0), BigDecimal.valueOf(1500), "버스 이용", user, "현금", CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 7, 0, 0), BigDecimal.valueOf(8000), "점심 도시락", user, "카카오페이", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 8, 0, 0), BigDecimal.valueOf(50000), "콘서트 티켓", user, "신용카드", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 9, 0, 0), BigDecimal.valueOf(7000), "약국 구매", user, "체크카드", CategoryName.HEALTH),
                new Expense(LocalDateTime.of(2024, 11, 10, 0, 0), BigDecimal.valueOf(15000), "브런치 카페", user, "카카오페이", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 11, 0, 0), BigDecimal.valueOf(12000), "택시 이용", user, "현금", CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 12, 0, 0), BigDecimal.valueOf(35000), "저녁 회식", user, "신용카드", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 13, 0, 0), BigDecimal.valueOf(20000), "생필품 구매", user, "체크카드", CategoryName.LIFESTYLE),
                new Expense(LocalDateTime.of(2024, 11, 14, 0, 0), BigDecimal.valueOf(15000), "책 구매", user, "카카오페이", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 15, 0, 0), BigDecimal.valueOf(5000), "아이스크림", user, "현금", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 16, 0, 0), BigDecimal.valueOf(25000), "주말 저녁 외식", user, "신용카드", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 17, 0, 0), BigDecimal.valueOf(45000), "기차 이용", user, "체크카드", CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 18, 0, 0), BigDecimal.valueOf(80000), "신발 구매", user, "카카오페이", CategoryName.FASHION),
                new Expense(LocalDateTime.of(2024, 11, 19, 0, 0), BigDecimal.valueOf(5000), "아침 식사", user, "현금", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 20, 0, 0), BigDecimal.valueOf(15000), "미술관 관람", user, "신용카드", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 21, 0, 0), BigDecimal.valueOf(20000), "토익 교재", user, "카카오페이", CategoryName.EDUCATION),
                new Expense(LocalDateTime.of(2024, 11, 22, 0, 0), BigDecimal.valueOf(4000), "커피", user, "현금", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 23, 0, 0), BigDecimal.valueOf(12000), "점심 외식", user, "신용카드", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 24, 0, 0), BigDecimal.valueOf(45000), "친구들과 술자리", user, "체크카드", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 25, 0, 0), BigDecimal.valueOf(10000), "청소 용품 구매", user, "카카오페이", CategoryName.LIFESTYLE),
                new Expense(LocalDateTime.of(2024, 11, 26, 0, 0), BigDecimal.valueOf(30000), "정기 검진", user, "현금", CategoryName.HEALTH),
                new Expense(LocalDateTime.of(2024, 11, 27, 0, 0), BigDecimal.valueOf(12000), "액세서리 구매", user, "신용카드", CategoryName.FASHION),
                new Expense(LocalDateTime.of(2024, 11, 28, 0, 0), BigDecimal.valueOf(8000), "야식", user, "체크카드", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 29, 0, 0), BigDecimal.valueOf(7000), "디저트 카페", user, "카카오페이", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 30, 0, 0), BigDecimal.valueOf(30000), "연극 관람", user, "현금", CategoryName.ENTERTAINMENT)
        );

        // 데이터 저장
        expenseRepository.saveAll(expenses);
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
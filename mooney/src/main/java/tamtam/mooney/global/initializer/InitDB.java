package tamtam.mooney.global.initializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitDB {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final CategoryBudgetRepository categoryBudgetRepository;
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
    public void initCategoryBudget() {
        User user = userService.getCurrentUser();
        String currentMonth =  LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        if (categoryBudgetRepository.findByUserAndPeriod(user, currentMonth).isEmpty()) {
            Map<CategoryName, BigDecimal> defaultBudget = Map.of(
                    CategoryName.FOOD, BigDecimal.valueOf(350000),
                    CategoryName.TRANSPORT, BigDecimal.valueOf(70000),
                    CategoryName.CAFE, BigDecimal.valueOf(25000),
                    CategoryName.ENTERTAINMENT, BigDecimal.valueOf(100000),
                    CategoryName.FASHION, BigDecimal.valueOf(90000),
                    CategoryName.BEAUTY, BigDecimal.valueOf(50000),
                    CategoryName.LIFESTYLE, BigDecimal.valueOf(100000),
                    CategoryName.HEALTH, BigDecimal.valueOf(30000),
                    CategoryName.EDUCATION, BigDecimal.valueOf(35000)
            );

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            String currentPeriod = LocalDate.now().format(formatter);

            // 디폹트 예산 생성 (테스트용)
            defaultBudget.forEach((category, amount) -> {
                CategoryBudget categoryBudget = new CategoryBudget();
                categoryBudget.setCategoryName(category);
                categoryBudget.setAmount(amount);
                categoryBudget.setPeriod(currentPeriod);
                categoryBudget.setUser(userService.getCurrentUser());
                categoryBudgetRepository.save(categoryBudget);
            });
        }
    }
    @PostConstruct
    public void initExpense() {
        try {
            // 현재 사용자 가져오기
            User user = userService.getCurrentUser();
            if (user == null) {
                throw new IllegalStateException("현재 사용자를 찾을 수 없습니다.");
            }

            // 이번 달의 첫 번째 날짜와 마지막 날짜 계산
            LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1); // 현재 월의 첫 날
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth()); // 현재 월의 마지막 날

            // 날짜 범위와 사용자의 정보를 로그로 출력
            log.info("사용자 {}의 이번 달 지출 확인: {}부터 {}까지", user.getNickname(), startOfMonth, endOfMonth);

            LocalDateTime startDateTime = startOfMonth.atStartOfDay(); // 첫 번째 날짜의 00:00시로 변환
            LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59, 999999999); // 마지막 날짜의 마지막 순간 (23:59:59.999999999)

            // 이번 달의 총 지출 금액 가져오기
            BigDecimal totalExpenseThisMonth = expenseRepository.findTotalExpenseAmountByUserAndMonth(user, startDateTime, endDateTime);

            // 이번 달에 지출이 있으면 초기화 작업을 하지 않음
            if (totalExpenseThisMonth.compareTo(BigDecimal.ZERO) != 0) {
                log.info("이번 달에 지출이 이미 존재하므로 초기화 작업을 건너뜁니다.");
                return;
            }
            // JSON 데이터를 Expense 객체로 변환하여 삽입

            // Expense 데이터
            List<Expense> expenses = List.of(
                    new Expense(LocalDateTime.of(2024, 12, 1, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "카카오페이", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 1, 0, 0), BigDecimal.valueOf(2500), "지하철 이용", user, "체크카드", CategoryName.TRANSPORT),
                    new Expense(LocalDateTime.of(2024, 12, 2, 0, 0), BigDecimal.valueOf(8000), "커피와 디저트", user, "카카오페이", CategoryName.CAFE),
                    new Expense(LocalDateTime.of(2024, 12, 2, 0, 0), BigDecimal.valueOf(12000), "영화 관람", user, "신용카드", CategoryName.ENTERTAINMENT),
                    new Expense(LocalDateTime.of(2024, 12, 3, 0, 0), BigDecimal.valueOf(35000), "의류 구매", user, "체크카드", CategoryName.FASHION),
                    new Expense(LocalDateTime.of(2024, 12, 4, 0, 0), BigDecimal.valueOf(20000), "저녁 외식", user, "현금", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 5, 0, 0), BigDecimal.valueOf(15000), "헤어 컷", user, "카카오페이", CategoryName.BEAUTY),
                    new Expense(LocalDateTime.of(2024, 12, 6, 0, 0), BigDecimal.valueOf(1500), "버스 이용", user, "교통카드", CategoryName.TRANSPORT),
                    new Expense(LocalDateTime.of(2024, 12, 7, 0, 0), BigDecimal.valueOf(8000), "점심 도시락", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 8, 0, 0), BigDecimal.valueOf(50000), "콘서트 티켓", user, "신용카드", CategoryName.ENTERTAINMENT),
                    new Expense(LocalDateTime.of(2024, 12, 8, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "카카오페이", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 8, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "현금", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 9, 0, 0), BigDecimal.valueOf(7000), "약국 구매", user, "체크카드", CategoryName.HEALTH),
                    new Expense(LocalDateTime.of(2024, 12, 10, 0, 0), BigDecimal.valueOf(15000), "브런치 카페", user, "카카오페이", CategoryName.CAFE),
                    new Expense(LocalDateTime.of(2024, 12, 11, 0, 0), BigDecimal.valueOf(12000), "택시 이용", user, "카드", CategoryName.TRANSPORT),
                    new Expense(LocalDateTime.of(2024, 12, 12, 0, 0), BigDecimal.valueOf(35000), "저녁 회식", user, "현금", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 13, 0, 0), BigDecimal.valueOf(20000), "생필품 구매", user, "카카오페이", CategoryName.LIFESTYLE),
                    new Expense(LocalDateTime.of(2024, 12, 13, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 14, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "신용카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 14, 0, 0), BigDecimal.valueOf(15000), "책 구매", user, "카드", CategoryName.ENTERTAINMENT),
                    new Expense(LocalDateTime.of(2024, 12, 14, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 14, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "현금", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 15, 0, 0), BigDecimal.valueOf(5000), "아이스크림", user, "카카오페이", CategoryName.CAFE),
                    new Expense(LocalDateTime.of(2024, 12, 16, 0, 0), BigDecimal.valueOf(25000), "주말 저녁 외식", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 17, 0, 0), BigDecimal.valueOf(45000), "기차 이용", user, "카드", CategoryName.TRANSPORT),
                    new Expense(LocalDateTime.of(2024, 12, 18, 0, 0), BigDecimal.valueOf(80000), "신발 구매", user, "신용카드", CategoryName.FASHION),
                    new Expense(LocalDateTime.of(2024, 12, 18, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 18, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "현금", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 19, 0, 0), BigDecimal.valueOf(5000), "아침 식사", user, "카카오페이", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 20, 0, 0), BigDecimal.valueOf(15000), "미술관 관람", user, "카드", CategoryName.ENTERTAINMENT),
                    new Expense(LocalDateTime.of(2024, 12, 20, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 20, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "현금", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 21, 0, 0), BigDecimal.valueOf(20000), "토익 교재", user, "카드", CategoryName.EDUCATION),
                    new Expense(LocalDateTime.of(2024, 12, 21, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 21, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "카카오페이", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 22, 0, 0), BigDecimal.valueOf(4000), "커피", user, "현금", CategoryName.CAFE),
                    new Expense(LocalDateTime.of(2024, 12, 23, 0, 0), BigDecimal.valueOf(12000), "점심 외식", user, "카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 24, 0, 0), BigDecimal.valueOf(45000), "친구들과 술자리", user, "카카오페이", CategoryName.ENTERTAINMENT),
                    new Expense(LocalDateTime.of(2024, 12, 25, 0, 0), BigDecimal.valueOf(10000), "청소 용품 구매", user, "체크카드", CategoryName.LIFESTYLE),
                    new Expense(LocalDateTime.of(2024, 12, 25, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 25, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 26, 0, 0), BigDecimal.valueOf(30000), "정기 검진", user, "체크카드", CategoryName.HEALTH),
                    new Expense(LocalDateTime.of(2024, 12, 26, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 26, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 27, 0, 0), BigDecimal.valueOf(12000), "액세서리 구매", user, "체크카드", CategoryName.FASHION),
                    new Expense(LocalDateTime.of(2024, 12, 28, 0, 0), BigDecimal.valueOf(8000), "야식", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 29, 0, 0), BigDecimal.valueOf(7000), "디저트 카페", user, "체크카드", CategoryName.CAFE),
                    new Expense(LocalDateTime.of(2024, 12, 29, 0, 0), BigDecimal.valueOf(12000), "점심 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 29, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", user, "체크카드", CategoryName.FOOD),
                    new Expense(LocalDateTime.of(2024, 12, 30, 0, 0), BigDecimal.valueOf(30000), "연극 관람", user, "체크카드", CategoryName.ENTERTAINMENT)
            );

            log.info("이번 달의 총 지출 금액: {}", totalExpenseThisMonth);

            // 데이터 저장
            expenseRepository.saveAll(expenses);
        } catch (Exception e) {
            // 예외 발생 시 로그로 출력하고 다시 예외를 던짐
            log.error("지출 초기화 중 오류 발생", e);
            throw e;
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

        UserSchedule schedule1 = UserSchedule.builder()
                .title("카페에서 시험공부")
                .startDateTime(startOfTomorrow.plusHours(10))
                .endDateTime(startOfTomorrow.plusHours(11))
                .location("투썸플레이스")
                .user(user)
                .build();
        schedule1.setCategoryNameAndPredictedAmount(CategoryName.EDUCATION, BigDecimal.valueOf(5000L));
        defaultSchedules.add(schedule1);

        UserSchedule schedule2 = UserSchedule.builder()
                .title("3시반 미술 전시회")
                .startDateTime(startOfTomorrow.plusHours(14))
                .endDateTime(startOfTomorrow.plusHours(16))
                .location("그라운드시소 서촌")
                .user(user)
                .build();
        schedule2.setCategoryNameAndPredictedAmount(CategoryName.ENTERTAINMENT, BigDecimal.valueOf(15000L));
        defaultSchedules.add(schedule2);

        // 세 번째 스케줄 추가
        UserSchedule schedule3 = UserSchedule.builder()
                .title("학원 등록")
                .startDateTime(startOfTomorrow.plusHours(18))
                .endDateTime(startOfTomorrow.plusHours(19))
                .location("실용음악학원")
                .isRepeating(true)
                .repeatType("MONTHLY")
                .user(user)
                .build();
        schedule3.setCategoryNameAndPredictedAmount(CategoryName.EDUCATION, BigDecimal.valueOf(200000L));
        defaultSchedules.add(schedule3);

        // Save all default schedules
        userScheduleRepository.saveAll(defaultSchedules);
    }

    private void createAndSaveDefaultNextMonthSchedules(User user) {
        List<UserSchedule> nextMonthSchedules = new ArrayList<>();

        // 다음달의 첫 번째 일정: 친구들이랑 여행
        UserSchedule nmSchedule1 = UserSchedule.builder()
                .title("친구들이랑 여행")
                .startDateTime(LocalDate.of(2025, 1, 15).atStartOfDay().plusHours(5))
                .endDateTime(LocalDate.of(2025, 1, 16).atStartOfDay().plusHours(18)) // 예시로 5시간 추가
                .location("속초")
                .user(user)
                .build();
        nmSchedule1.setCategoryNameAndPredictedAmount(CategoryName.TRAVEL, BigDecimal.ZERO);


        // 다음달의 두 번째 일정: TOEIC 시험 준비
        UserSchedule nmSchedule2 = UserSchedule.builder()
                .title("TOEIC 시험 준비")
                .startDateTime(LocalDate.of(2025, 1, 5).atTime(10, 0))
                .endDateTime(LocalDate.of(2025, 1, 5).atTime(12, 0))  // 예시로 2시간 추가
                .location("온라인")
                .user(user)
                .build();
        nmSchedule2.setCategoryNameAndPredictedAmount(CategoryName.EDUCATION, BigDecimal.ZERO);

        // 리스트에 일정 추가
        nextMonthSchedules.add(nmSchedule1);
        nextMonthSchedules.add(nmSchedule2);
        userScheduleRepository.saveAll(nextMonthSchedules);
    }
}

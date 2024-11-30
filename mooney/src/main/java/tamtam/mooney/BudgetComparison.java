package tamtam.mooney;

import tamtam.mooney.domain.entity.CategoryName;
import tamtam.mooney.domain.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetComparison {

    public static void main(String[] args) {
        // 예산 데이터
        Map<CategoryName, BigDecimal> currentBudget = Map.of(
                CategoryName.FOOD, BigDecimal.valueOf(300000),
                CategoryName.TRANSPORT, BigDecimal.valueOf(70000),
                CategoryName.CAFE, BigDecimal.valueOf(25000),
                CategoryName.ENTERTAINMENT, BigDecimal.valueOf(100000),
                CategoryName.FASHION, BigDecimal.valueOf(90000),
                CategoryName.BEAUTY, BigDecimal.valueOf(50000),
                CategoryName.LIFESTYLE, BigDecimal.valueOf(100000),
                CategoryName.HEALTH, BigDecimal.valueOf(30000),
                CategoryName.EDUCATION, BigDecimal.valueOf(35000)
        );

        // Expense 데이터
        List<Expense> expenses = List.of(
                new Expense(LocalDateTime.of(2024, 11, 1, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 1, 0, 0), BigDecimal.valueOf(2500), "지하철 이용", null, "", CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 2, 0, 0), BigDecimal.valueOf(8000), "커피와 디저트", null, "", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 2, 0, 0), BigDecimal.valueOf(12000), "영화 관람", null, "", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 3, 0, 0), BigDecimal.valueOf(35000), "의류 구매", null, "", CategoryName.FASHION),
                new Expense(LocalDateTime.of(2024, 11, 4, 0, 0), BigDecimal.valueOf(20000), "저녁 외식",null, "",  CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 5, 0, 0), BigDecimal.valueOf(15000), "헤어 컷",null, "",  CategoryName.BEAUTY),
                new Expense(LocalDateTime.of(2024, 11, 6, 0, 0), BigDecimal.valueOf(1500), "버스 이용",null, "",  CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 7, 0, 0), BigDecimal.valueOf(8000), "점심 도시락",null, "",  CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 8, 0, 0), BigDecimal.valueOf(50000), "콘서트 티켓",null, "",  CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 8, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 8, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 9, 0, 0), BigDecimal.valueOf(7000), "약국 구매",null, "",  CategoryName.HEALTH),
                new Expense(LocalDateTime.of(2024, 11, 10, 0, 0), BigDecimal.valueOf(15000), "브런치 카페",null, "",  CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 11, 0, 0), BigDecimal.valueOf(12000), "택시 이용", null, "", CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 12, 0, 0), BigDecimal.valueOf(35000), "저녁 회식", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 13, 0, 0), BigDecimal.valueOf(20000), "생필품 구매",null, "",  CategoryName.LIFESTYLE),
                new Expense(LocalDateTime.of(2024, 11, 13, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 14, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 14, 0, 0), BigDecimal.valueOf(15000), "책 구매", null, "", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 14, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 14, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 15, 0, 0), BigDecimal.valueOf(5000), "아이스크림",null, "",  CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 16, 0, 0), BigDecimal.valueOf(25000), "주말 저녁 외식",null, "",  CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 17, 0, 0), BigDecimal.valueOf(45000), "기차 이용",null, "",  CategoryName.TRANSPORT),
                new Expense(LocalDateTime.of(2024, 11, 18, 0, 0), BigDecimal.valueOf(80000), "신발 구매", null, "", CategoryName.FASHION),
                new Expense(LocalDateTime.of(2024, 11, 18, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 18, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 19, 0, 0), BigDecimal.valueOf(5000), "아침 식사",null, "",  CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 20, 0, 0), BigDecimal.valueOf(15000), "미술관 관람", null, "", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 20, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 20, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 21, 0, 0), BigDecimal.valueOf(20000), "토익 교재", null, "", CategoryName.EDUCATION),
                new Expense(LocalDateTime.of(2024, 11, 21, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 21, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 22, 0, 0), BigDecimal.valueOf(4000), "커피", null, "", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 23, 0, 0), BigDecimal.valueOf(12000), "점심 외식", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 24, 0, 0), BigDecimal.valueOf(45000), "친구들과 술자리", null, "", CategoryName.ENTERTAINMENT),
                new Expense(LocalDateTime.of(2024, 11, 25, 0, 0), BigDecimal.valueOf(10000), "청소 용품 구매", null, "", CategoryName.LIFESTYLE),
                new Expense(LocalDateTime.of(2024, 11, 25, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 25, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 26, 0, 0), BigDecimal.valueOf(30000), "정기 검진", null, "", CategoryName.HEALTH),
                new Expense(LocalDateTime.of(2024, 11, 26, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 26, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 27, 0, 0), BigDecimal.valueOf(12000), "액세서리 구매",null, "",  CategoryName.FASHION),
                new Expense(LocalDateTime.of(2024, 11, 28, 0, 0), BigDecimal.valueOf(8000), "야식", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 29, 0, 0), BigDecimal.valueOf(7000), "디저트 카페", null, "", CategoryName.CAFE),
                new Expense(LocalDateTime.of(2024, 11, 29, 0, 0), BigDecimal.valueOf(12000), "점심 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 29, 0, 0), BigDecimal.valueOf(10000), "저녁 식사", null, "", CategoryName.FOOD),
                new Expense(LocalDateTime.of(2024, 11, 30, 0, 0), BigDecimal.valueOf(30000), "연극 관람", null, "", CategoryName.ENTERTAINMENT)
        );

        // 카테고리별 지출 합산
        Map<CategoryName, BigDecimal> categoryExpenses = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategoryName,
                        Collectors.mapping(Expense::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        // 전체 지출 금액 합산
        BigDecimal totalExpense = categoryExpenses.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 전체 예산 금액 합산
        BigDecimal totalBudget = currentBudget.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 전체 지출 금액 출력
        System.out.println("전체 지출 금액: " + totalExpense);
        System.out.println("\n전체 예산 금액: " + totalBudget);

        // 출력: 카테고리별 예산 vs 지출
        System.out.println("카테고리별 예산과 지출 비교");
        System.out.println("==================================");
        for (CategoryName category : CategoryName.values()) {
            BigDecimal expenseAmount = categoryExpenses.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal budgetAmount = currentBudget.getOrDefault(category, BigDecimal.valueOf(0));
            System.out.printf("%-20s | 예산: %,10d | 지출: %,10d\n", category.getDescription(), budgetAmount.intValue(), expenseAmount.intValue());
        }
    }
}

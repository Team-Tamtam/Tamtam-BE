package tamtam.mooney.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TestInputService {
    private final AIPromptService aiPromptService;

    public String generateThisMonthReport() {
        double totalBudget = 800000;

        Map<String, Integer> categoryBudgets = new HashMap<>();
        categoryBudgets.put("경조/선물", 0);
        categoryBudgets.put("교육/학습", 32000);
        categoryBudgets.put("교통", 80000);
        categoryBudgets.put("금융", 0);
        categoryBudgets.put("문화/여가", 120000);
        categoryBudgets.put("반려동물", 0);
        categoryBudgets.put("뷰티/미용", 40000);
        categoryBudgets.put("생활", 40000);
        categoryBudgets.put("술/유흥", 24000);
        categoryBudgets.put("식비", 240000);
        categoryBudgets.put("여행/숙박", 0);
        categoryBudgets.put("온라인 쇼핑", 0);
        categoryBudgets.put("의료/건강", 56000);
        categoryBudgets.put("자녀/육아", 0);
        categoryBudgets.put("자동차", 0);
        categoryBudgets.put("주거/통신", 0);
        categoryBudgets.put("카페/간식", 64000);
        categoryBudgets.put("패션/쇼핑", 24000);

        // 3. 월별 지출(monthlyExpenses) 리스트 생성
        List<Map<String, Object>> monthlyExpenses = new ArrayList<>();

        // 지출 데이터 추가
        monthlyExpenses.add(Map.of("date", "2024-11-01", "category", "식비", "description", "점심 식사", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-01", "category", "교통", "description", "지하철 이용", "amount", 2500));
        monthlyExpenses.add(Map.of("date", "2024-11-02", "category", "카페/간식", "description", "커피와 디저트", "amount", 8000));
        monthlyExpenses.add(Map.of("date", "2024-11-02", "category", "문화/여가", "description", "영화 관람", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-03", "category", "쇼핑", "description", "의류 구매", "amount", 35000));
        monthlyExpenses.add(Map.of("date", "2024-11-04", "category", "식비", "description", "저녁 외식", "amount", 20000));
        monthlyExpenses.add(Map.of("date", "2024-11-05", "category", "뷰티/미용", "description", "헤어 컷", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-06", "category", "교통", "description", "버스 이용", "amount", 1500));
        monthlyExpenses.add(Map.of("date", "2024-11-07", "category", "식비", "description", "점심 도시락", "amount", 8000));
        monthlyExpenses.add(Map.of("date", "2024-11-08", "category", "문화/여가", "description", "콘서트 티켓", "amount", 50000));
        monthlyExpenses.add(Map.of("date", "2024-11-09", "category", "의료/건강", "description", "약국 구매", "amount", 7000));
        monthlyExpenses.add(Map.of("date", "2024-11-10", "category", "카페/간식", "description", "브런치 카페", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-11", "category", "교통", "description", "택시 이용", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-12", "category", "식비", "description", "저녁 회식", "amount", 35000));
        monthlyExpenses.add(Map.of("date", "2024-11-13", "category", "생활", "description", "생필품 구매", "amount", 20000));
        monthlyExpenses.add(Map.of("date", "2024-11-14", "category", "쇼핑", "description", "책 구매", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-15", "category", "카페/간식", "description", "아이스크림", "amount", 5000));
        monthlyExpenses.add(Map.of("date", "2024-11-16", "category", "식비", "description", "주말 저녁 외식", "amount", 25000));
        monthlyExpenses.add(Map.of("date", "2024-11-17", "category", "교통", "description", "기차 이용", "amount", 45000));
        monthlyExpenses.add(Map.of("date", "2024-11-18", "category", "패션/쇼핑", "description", "신발 구매", "amount", 80000));
        monthlyExpenses.add(Map.of("date", "2024-11-19", "category", "식비", "description", "아침 식사", "amount", 5000));
        monthlyExpenses.add(Map.of("date", "2024-11-20", "category", "문화/여가", "description", "미술관 관람", "amount", 15000));
        monthlyExpenses.add(Map.of("date", "2024-11-21", "category", "교육/학습", "description", "토익 교재", "amount", 20000));
        monthlyExpenses.add(Map.of("date", "2024-11-22", "category", "카페/간식", "description", "커피", "amount", 4000));
        monthlyExpenses.add(Map.of("date", "2024-11-23", "category", "식비", "description", "점심 외식", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-24", "category", "술/유흥", "description", "친구들과 술자리", "amount", 45000));
        monthlyExpenses.add(Map.of("date", "2024-11-25", "category", "생활", "description", "청소 용품 구매", "amount", 10000));
        monthlyExpenses.add(Map.of("date", "2024-11-26", "category", "의료/건강", "description", "정기 검진", "amount", 30000));
        monthlyExpenses.add(Map.of("date", "2024-11-27", "category", "패션/쇼핑", "description", "액세서리 구매", "amount", 12000));
        monthlyExpenses.add(Map.of("date", "2024-11-28", "category", "식비", "description", "야식", "amount", 8000));
        monthlyExpenses.add(Map.of("date", "2024-11-29", "category", "카페/간식", "description", "디저트 카페", "amount", 7000));
        monthlyExpenses.add(Map.of("date", "2024-11-30", "category", "문화/여가", "description", "연극 관람", "amount", 30000));

        return aiPromptService.buildThisMonthReportMessages(totalBudget, categoryBudgets, monthlyExpenses);
    }
}

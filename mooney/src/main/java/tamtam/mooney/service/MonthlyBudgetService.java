package tamtam.mooney.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.repository.MonthlyBudgetRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;

}

package tamtam.mooney.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.entity.MonthlyReport;
import tamtam.mooney.domain.entity.User;

import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    Optional<MonthlyReport> findByUserAndPeriod(User user, String period);
}

package tamtam.mooney.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.entity.MonthlyReport;
import tamtam.mooney.entity.User;

import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    Optional<MonthlyReport> findByUserAndPeriod(User user, String period);
}

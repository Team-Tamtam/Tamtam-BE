package tamtam.mooney.global.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tamtam.mooney.global.initializer.InitDB;

@Component
@RequiredArgsConstructor
public class InitUserScheduleScheduler {
    private final InitDB initDB;

    @Scheduled(cron = "0 0 0 * * ?")
    public void initSchedules() {
        initDB.initUserSchedules();
    }
}

package tamtam.mooney.domain.dto.request;

import javax.validation.constraints.NotNull;
import java.util.List;

public record DailyBudgetRequestDto (
        @NotNull List<Long> scheduleIds
){ }
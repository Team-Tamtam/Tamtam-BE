package tamtam.mooney.global.openai.dto;

public record NextMonthScheduleInputDto (
        String event,
        int estimatedCost
) {}
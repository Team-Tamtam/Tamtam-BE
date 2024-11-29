package tamtam.mooney.domain.dto;

import tamtam.mooney.domain.entity.UserSchedule;

import java.util.List;

public class MergeSchedulesRequest {
    private List<UserSchedule> repeatedSchedules;
    private List<UserSchedule> tomorrowSchedules;
    private String jsonResponse;

    // Getters and setters
    public List<UserSchedule> getRepeatedSchedules() {
        return repeatedSchedules;
    }

    public void setRepeatedSchedules(List<UserSchedule> repeatedSchedules) {
        this.repeatedSchedules = repeatedSchedules;
    }

    public List<UserSchedule> getTomorrowSchedules() {
        return tomorrowSchedules;
    }

    public void setTomorrowSchedules(List<UserSchedule> tomorrowSchedules) {
        this.tomorrowSchedules = tomorrowSchedules;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }
}

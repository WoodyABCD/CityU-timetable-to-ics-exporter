public class Timeslot {
    private String startTime, endTime, startDate, endDate, weekday, location;

    public Timeslot(String startTime, String endTime, String startDate, String endDate, String weekday,
            String location) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weekday = weekday;
        this.location = location;
    }

    public String toString() {
        return String.format("Day: %s\nTime: %s - %s\nDate: %s - %s\nLocation: %s", weekday, startTime, endTime,
                startDate, endDate, location);
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}

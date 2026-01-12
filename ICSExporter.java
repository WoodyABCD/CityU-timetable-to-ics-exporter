import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.UUID;

public class ICSExporter {
    StringBuilder builder;

    public ICSExporter() {
        builder = new StringBuilder();
        // 1. Start the Calendar Container
        builder.append("BEGIN:VCALENDAR\r\n");
        builder.append("VERSION:2.0\r\n");
        builder.append("PRODID:-//My App//Multi Events//EN\r\n");
        builder.append("METHOD:PUBLISH\r\n");
    }

    public ICSExporter(ArrayList<Course> courses) {
        this();
        for (Course c : courses) {
            for (Timeslot t : c.getTimeslots()) {
                addWeeklyEvent(t.getLocation() + "(" + c.getId() + ")",
                        t.getStartDate(), t.getStartTime(),
                        t.getStartDate(), t.getEndTime(),
                        t.getWeekday(),
                        t.getEndDate(), t.getEndTime());
            }
        }
    }

    public ICSExporter(Course course) {
        this();
        for (Timeslot t : course.getTimeslots()) {
            addWeeklyEvent(t.getLocation() + "(" + course.getId() + ")",
                    t.getStartDate(), t.getStartTime(),
                    t.getStartDate(), t.getEndTime(),
                    t.getWeekday(),
                    t.getEndDate(), t.getEndTime());
        }
    }

    /**
     * Export the built ICS content to a file.
     * 
     * @param filename
     */
    public void export(String filename) {
        builder.append("END:VCALENDAR\r\n");

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(builder.toString());
            System.out.println("File '" + filename + "' created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param title
     * @param startDay
     * @param startTime
     * @param endDay
     * @param endTime
     */
    public void addSingleEvent(String title, String startDay, String startTime, String endDay, String endTime) {

        addEvent(title, DataConverter.formatToUtc(startDay, startTime), DataConverter.formatToUtc(endDay, endTime), "");
    }

    /**
     * Add a weekly recurring event.
     * 
     * @param title
     * @param startDay
     * @param startTime
     * @param endDay
     * @param endTime
     * @param repeatDay
     * @param endRepeatDay
     * @param endRepeatTime
     */
    public void addWeeklyEvent(String title, String startDay, String startTime, String endDay, String endTime,
            String repeatDay, String endRepeatDay, String endRepeatTime) {
        repeatDay = repeatDay.toUpperCase();

        String[] days = { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };
        String[] byday = { "MO", "TU", "WE", "TH", "FR", "SA", "SU" };
        try {
            int idx = -1;
            for (int i = 0; i < days.length; i++) {
                if (days[i].equals(repeatDay)) {
                    idx = i;
                    break;
                }
            }
            String day = byday[idx];
            // formatToUtc(startDay, startTime);
            startDay = nearby(startDay, repeatDay);
            endDay = nearby(endDay, repeatDay);

            String rrule = "FREQ=WEEKLY;BYDAY=" + day + ";UNTIL="
                    + DataConverter.formatToUtc(endRepeatDay, endRepeatTime);
            addEvent(title, DataConverter.formatToUtc(startDay, startTime), DataConverter.formatToUtc(endDay, endTime),
                    rrule);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No such week day");
        }
    }

    private String nearby(String startDay, String repeatDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
        LocalDate date = LocalDate.parse(startDay, formatter);
        DayOfWeek targetDay = DayOfWeek.MONDAY; // Default

        switch (repeatDay) {
            case "MON":
                targetDay = DayOfWeek.MONDAY;
                break;
            case "TUE":
                targetDay = DayOfWeek.TUESDAY;
                break;
            case "WED":
                targetDay = DayOfWeek.WEDNESDAY;
                break;
            case "THU":
                targetDay = DayOfWeek.THURSDAY;
                break;
            case "FRI":
                targetDay = DayOfWeek.FRIDAY;
                break;
            case "SAT":
                targetDay = DayOfWeek.SATURDAY;
                break;
            case "SUN":
                targetDay = DayOfWeek.SUNDAY;
                break;
        }

        LocalDate adjustedDate = date.with(TemporalAdjusters.nextOrSame(targetDay));

        return adjustedDate.format(formatter);
    }

    public void addEvent(String title, String start, String end, String rrule) {
        // LocalDateTime startDT = LocalDateTime.of(2026, 1, 20, 10, 0); // Monday
        // LocalDateTime endDT = LocalDateTime.of(2026, 1, 20, 11, 0);
        // RRULE: Weekly on Mondays
        // String rule = "FREQ=WEEKLY;BYDAY=MO";
        builder.append("BEGIN:VEVENT\r\n");

        // CRITICAL: Generate a new unique ID for every single event
        builder.append("UID:").append(UUID.randomUUID().toString()).append("\r\n");
        builder.append("DTSTAMP:").append(DataConverter.formatToUtc(LocalDateTime.now())).append("\r\n");

        builder.append("DTSTART:").append(start).append("\r\n");
        builder.append("DTEND:").append(end).append("\r\n");

        if (rrule != null && !rrule.isEmpty()) {
            builder.append("RRULE:").append(rrule).append("\r\n");
        }

        builder.append("SUMMARY:").append(title).append("\r\n");
        builder.append("END:VEVENT\r\n");
    }

}
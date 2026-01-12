import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class DataConverter {
    public static String convertWeekday(String inputTime) {
        switch (inputTime) {
            case "M":
                return "MON";
            case "T":
                return "TUE";
            case "W":
                return "WED";
            case "R":
                return "THU";
            case "F":
                return "FRI";
            case "S":
                return "SAT";
            default:
                return "SUN";
        }
    }

    /**
     * Convert location from "Yeung Y5303" to "AC1-Y5303"
     * 
     * @param inputLocation
     * @return
     */
    public static String convertLocation(String inputLocation) {
        String[] temp = inputLocation.split(" ");
        String result = "";
        switch (temp[0].toUpperCase()) {
            case "YEUNG":
                result = "AC1";
                break;
            case "BANK":
                result = "BOC";
                break;
            case "LI":
                result = "AC2";
                break;
            case "R":
                result = "CMC";
                break;
            default:
                result = "Unknown location";
                break;
        }
        if (temp.length > 1)
            result += "-" + temp[temp.length - 1];
        return result;
    }

    /**
     * Convert date from "Mar 2, 2026" to "2026/3/2"
     * 
     * @param inputDate
     * @return
     */
    public static String convertDate(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

        // 3. Convert
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);
        String result = date.format(outputFormatter);

        return result;
    }

    /**
     * Convert time from "10:00 PM" to "22:00"
     * 
     * @param inputTime
     * @return
     */
    public static String convertTime(String inputTime) {
        DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("h:mm a")
                .toFormatter(Locale.ENGLISH);

        // 2. Define Output Format (24-hour clock)
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 3. Parse and Convert
        LocalTime time = LocalTime.parse(inputTime, inputFormatter);
        String militaryTime = time.format(outputFormatter);
        return militaryTime;
    }

    /**
     * Convert LocalDateTime to UTC format string
     * 
     * @param date
     * @return
     */
    public static String formatToUtc(LocalDateTime date) {
        return date.atZone(java.time.ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    }

    public static String formatToUtc(String day, String time) {
        LocalDateTime dt;
        String[] dayArr = day.split("/");
        String[] timeArr = time.split(":");
        int[] dayIntArr = new int[dayArr.length];
        for (int i = 0; i < dayIntArr.length; i++) {
            dayIntArr[i] = Integer.parseInt(dayArr[i]);
        }
        int[] timeIntArr = new int[timeArr.length];
        for (int i = 0; i < timeIntArr.length; i++) {
            timeIntArr[i] = Integer.parseInt(timeArr[i]);
        }
        dt = LocalDateTime.of(dayIntArr[0], dayIntArr[1], dayIntArr[2], timeIntArr[0], timeIntArr[1]);
        return formatToUtc(dt);
    }
}

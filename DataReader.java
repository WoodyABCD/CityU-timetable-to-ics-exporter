import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DataReader {
    // private String courses = "";
    // private String crns = "";
    private ArrayList<Course> coursesAry;

    public DataReader(String data, String program) {
        Scanner input = null;
        coursesAry = new ArrayList<>();
        program = program.toUpperCase();
        try {
            input = new Scanner(new File(data));
            while (input.hasNext()) {
                String line = input.nextLine();
                // if (line.contains(program) || line.contains("GE") || line.contains("CB")) {
                searchDetail(line, input);
                // }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    private void searchDetail(String course, Scanner input) {
        String line = "";
        String[] courseArr = course.split("-");
        String courseId = courseArr[1].trim().replace(" ", "");
        // courses += courseId + "-";
        Course c = new Course(courseId, courseArr[2]);
        String crn = "";
        do {
            try {
                line = input.nextLine();

                if (line.contains("CRN")) {
                    String[] temp = line.split(":");
                    crn = temp[1].trim();
                    c.setCrn(crn);
                }
                if (line.contains("Class")) {
                    String temp[] = line.split("\t");
                    String temp2[] = temp[1].split(" - ");
                    String startTime = convertTime(temp2[0]);
                    String endTime = convertTime(temp2[1]);
                    String weekday = convertWeekday(temp[2]);
                    String[] temp3 = temp[4].split(" - ");
                    String startDate = convertDate(temp3[0]);
                    String endDate = convertDate(temp3[1]);
                    String location = convertLocation(temp[3]);
                    Timeslot t = new Timeslot(startTime, endTime, startDate, endDate, weekday, location);
                    c.addTimeslot(t);
                    // System.out.println(line);
                }
            } catch (Exception e) {
                break;
            }
        } while (line != "");
        coursesAry.add(c);
    }

    private boolean hasCourse(Course c) {
        boolean result = false;
        for (Course course : coursesAry) {
            if (course.isEqualTo(c))
                return true;
        }
        return result;
    }

    private String convertWeekday(String inputTime) {
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

    private String convertLocation(String inputLocation) {
        String[] temp = inputLocation.split(" ");
        String result = "";
        switch (temp[0]) {
            case "Yeung":
                result = "AC1";
                break;
            case "Bank":
                result = "BOC";
                break;
            case "Li":
                result = "AC2";
                break;
            case "R":
                result = "CMC";
                break;
            default:
                result = "Unknown location";
                break;
        }
        result += "-" + temp[temp.length - 1];
        return result;
    }

    private String convertDate(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

        // 3. Convert
        LocalDate date = LocalDate.parse(inputDate, inputFormatter);
        String result = date.format(outputFormatter);

        return result;
    }

    private String convertTime(String inputTime) {
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

    public String getCourseIds() {
        String result = "";
        for (Course course : coursesAry) {
            if (!result.contains(course.getId()))
                result += course.getId() + ",";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }

    public String getCoursesJSON() {
        String result = "";
        for (Course course : coursesAry) {
            result += course + ",\n";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }

    public String getCourseCrns() {
        String result = "";
        for (Course course : coursesAry) {
            result += course.getCrn() + ",";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }

    public ArrayList<Course> getCoursesAry() {
        return coursesAry;
    }

}

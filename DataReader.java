import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class DataReader {
    private ArrayList<Course> coursesAry;

    public DataReader(String data) throws FileNotFoundException {
        Scanner input = null;
        coursesAry = new ArrayList<>();
        // program = program.toUpperCase();
        try {
            input = new Scanner(new File(data));
            while (true) {
                String line = input.nextLine();
                if (line.contains("Total Credit Hours")) {
                    input.nextLine();
                    break;
                }
            }
            while (input.hasNext()) {
                String line = input.nextLine();
                if (line.contains("Return to Previous")) {
                    break;
                }
                // if (line.contains(program) || line.contains("GE") || line.contains("CB")) {
                searchDetail(line, input);
                // }
            }

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

        Course c = hasCourse(courseId);
        if (c == null) {
            c = new Course(courseId);
            coursesAry.add(c);
        }
        String crn = "";
        do {
            try {
                line = input.nextLine();

                if (line.contains("CRN")) {
                    String[] temp = line.split(":");
                    crn = temp[1].trim();
                }
                if (line.contains("Class")) {
                    String temp[] = line.split("\t");
                    String temp2[] = temp[1].split(" - ");
                    String startTime = DataConverter.convertTime(temp2[0]);
                    String endTime = DataConverter.convertTime(temp2[1]);
                    String weekday = DataConverter.convertWeekday(temp[2]);
                    String[] temp3 = temp[4].split(" - ");
                    String startDate = DataConverter.convertDate(temp3[0]);
                    String endDate = DataConverter.convertDate(temp3[1]);
                    String location = DataConverter.convertLocation(temp[3]);
                    Timeslot t = new Timeslot(crn, courseArr[2], startTime, endTime, startDate, endDate, weekday,
                            location);
                    c.addTimeslot(t);
                    // System.out.println(line);
                }
            } catch (Exception e) {
                break;
            }
        } while (line != "");
    }

    private Course hasCourse(String c) {
        for (Course course : coursesAry) {
            if (course.isEqualTo(c))
                return course;
        }
        return null;
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
            result += "\n-------------\n";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }

    public String getCourseCrns() {
        String result = "";
        for (Course course : coursesAry) {
            for (Timeslot t : course.getTimeslots())
                result += t.getCrn() + ",";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }

    public ArrayList<Course> getCoursesAry() {
        return coursesAry;
    }

}

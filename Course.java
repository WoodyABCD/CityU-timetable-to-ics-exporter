import java.util.ArrayList;

public class Course {
    private String crn, type, id;
    private ArrayList<Timeslot> timeslots;
    private Course preRequire;

    public Course(String id, String type) {
        this.id = id;
        this.type = type;
        this.timeslots = new ArrayList<>();
    }

    public Course(String id, String type, String crn, Course preRequire) {
        this(id, type);
        this.crn = crn;
        this.preRequire = preRequire;
    }

    public boolean isEqualTo(Course o) {
        return this.id.equals(o.id);
    }

    public boolean hasPreRequire() {
        return preRequire != null;
    }

    public void addTimeslot(Timeslot t) {
        timeslots.add(t);
    }

    public String getCrn() {
        return crn;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    @Override
    public String toString() {
        // String str = String.format("%s:{\n\stype:%s,\n\scrn:%s\n}", id, type, crn);
        String str = String.format("%s %s %s", id, type, crn);
        for (Timeslot t : timeslots) {
            str += "\n\t" + t.toString();
        }
        return str;
    }

    public ArrayList<Timeslot> getTimeslots() {
        return timeslots;
    }

}

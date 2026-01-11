public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("usage: java Main.java <filename>");
            System.exit(1);
        }
        String filename = args[0];
        DataReader rd = new DataReader(filename + ".txt", "CS");
        ICSExporter exporter = new ICSExporter(rd.getCoursesAry());
        exporter.export(filename + ".ics");
    }
}
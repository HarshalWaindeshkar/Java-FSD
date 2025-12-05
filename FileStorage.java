import java.io.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

/**
 * FileStorage.java
 * Handles reading/writing student and attendance CSV files.
 * Also creates default sample data if files are missing so app starts preloaded.
 */
public class FileStorage {

    private static final String STUDENT_FILE = "students.csv";
    private static final String ATTENDANCE_FILE = "attendance.csv";
    // attendance.csv format: date(YYYY-MM-DD),studentId,status(P/A)

    /**
     * Create default sample data files if they do not exist.
     * This ensures the app has preloaded students and attendance on first run.
     */
    public static void createDefaultDataIfMissing() throws IOException {
        File studentFile = new File(STUDENT_FILE);
        File attendanceFile = new File(ATTENDANCE_FILE);

        if (!studentFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(studentFile))) {
                pw.println("S001,Rahul Sharma");
                pw.println("S002,Neha Patil");
                pw.println("S003,Amit Verma");
                pw.println("S004,Priya Singh");
                pw.println("S005,Arjun Mehta");
                pw.println("S006,Simran Kaur");
                pw.println("S007,Rohan Deshmukh");
                pw.println("S008,Sneha Kulkarni");
                pw.println("S009,Vikram Joshi");
                pw.println("S010,Ananya Gupta");
            }
        }

        if (!attendanceFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(attendanceFile))) {
                // Day 1
                pw.println("2025-01-01,S001,P");
                pw.println("2025-01-01,S002,A");
                pw.println("2025-01-01,S003,P");
                pw.println("2025-01-01,S004,P");
                pw.println("2025-01-01,S005,A");
                pw.println("2025-01-01,S006,P");
                pw.println("2025-01-01,S007,P");
                pw.println("2025-01-01,S008,A");
                pw.println("2025-01-01,S009,P");
                pw.println("2025-01-01,S010,P");
                // Day 2
                pw.println("2025-01-02,S001,A");
                pw.println("2025-01-02,S002,P");
                pw.println("2025-01-02,S003,P");
                pw.println("2025-01-02,S004,P");
                pw.println("2025-01-02,S005,P");
                pw.println("2025-01-02,S006,P");
                pw.println("2025-01-02,S007,A");
                pw.println("2025-01-02,S008,P");
                pw.println("2025-01-02,S009,P");
                pw.println("2025-01-02,S010,P");
                // Day 3
                pw.println("2025-01-03,S001,P");
                pw.println("2025-01-03,S002,P");
                pw.println("2025-01-03,S003,A");
                pw.println("2025-01-03,S004,P");
                pw.println("2025-01-03,S005,A");
                pw.println("2025-01-03,S006,P");
                pw.println("2025-01-03,S007,P");
                pw.println("2025-01-03,S008,P");
                pw.println("2025-01-03,S009,A");
                pw.println("2025-01-03,S010,P");
            }
        }
    }

    public static List<Student> loadStudents() throws IOException {
        List<Student> list = new ArrayList<>();
        File f = new File(STUDENT_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", 2);
                if (p.length == 2) {
                    list.add(new Student(p[0].trim(), p[1].trim()));
                }
            }
        }
        return list;
    }

    public static void saveStudents(List<Student> students) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(STUDENT_FILE))) {
            for (Student s : students) {
                pw.println(s.getId() + "," + s.getName());
            }
        }
    }

    public static void saveAttendance(List<String[]> records) throws IOException {
        // records: each is {date, studentId, status}
        try (PrintWriter pw = new PrintWriter(new FileWriter(ATTENDANCE_FILE))) {
            for (String[] r : records) {
                pw.println(r[0] + "," + r[1] + "," + r[2]);
            }
        }
    }

    public static List<String[]> loadAttendanceAll() throws IOException {
        List<String[]> list = new ArrayList<>();
        File f = new File(ATTENDANCE_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", 3);
                if (p.length == 3) list.add(new String[]{p[0].trim(), p[1].trim(), p[2].trim()});
            }
        }
        return list;
    }

    public static List<String[]> getAttendanceForDate(String date) throws IOException {
        List<String[]> all = loadAttendanceAll();
        List<String[]> out = new ArrayList<>();
        for (String[] r : all) if (r[0].equals(date)) out.add(r);
        return out;
    }

    /**
     * Helper: convert a DefaultTableModel (student table) to a list of Student
     * Not strictly required by your app but can be useful if you decide to save directly from table.
     */
    public static List<Student> getStudentsFromTable(DefaultTableModel model) {
        List<Student> list = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object idObj = model.getValueAt(i, 0);
            Object nameObj = model.getValueAt(i, 1);
            if (idObj != null && nameObj != null) {
                list.add(new Student(idObj.toString(), nameObj.toString()));
            }
        }
        return list;
    }
}

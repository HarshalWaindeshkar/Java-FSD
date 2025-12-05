import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class AttendanceApp extends JFrame {

    private DefaultTableModel studentModel;    // shows student list with percentage
    private DefaultTableModel attendanceModel; // shows attendance for selected date
    private JTable tblStudents;
    private JTable tblAttendance;

    private JTextField txtId, txtName, txtDate;
    private JLabel lblPercentage;
    private List<Student> students = new ArrayList<>();

    public AttendanceApp(String username) {
        setTitle("Student Attendance System - Logged in as " + username);
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initMenu();
        initUI();

        loadInitialData();

        setVisible(true);
    }

    private void initMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem miSave = new JMenuItem("Save All");
        JMenuItem miLoad = new JMenuItem("Load All");
        JMenuItem miExport = new JMenuItem("Export Attendance (date)");
        JMenuItem miExit = new JMenuItem("Exit");

        miSave.addActionListener(e -> saveAll());
        miLoad.addActionListener(e -> loadInitialData());
        miExport.addActionListener(e -> exportAttendanceForDate());
        miExit.addActionListener(e -> System.exit(0));

        file.add(miSave);
        file.add(miLoad);
        file.add(miExport);
        file.addSeparator();
        file.add(miExit);

        JMenu help = new JMenu("Help");
        JMenuItem miAbout = new JMenuItem("About");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Student Attendance System\nJava Swing Project", "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(miAbout);

        mb.add(file);
        mb.add(help);
        setJMenuBar(mb);
    }

    private void initUI() {
        // Top panel - add student
        JPanel top = new JPanel(new GridLayout(2,1));
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        addPanel.setBorder(BorderFactory.createTitledBorder("Add Student"));

        addPanel.add(new JLabel("Student ID:"));
        txtId = new JTextField(8);
        addPanel.add(txtId);

        addPanel.add(new JLabel("Name:"));
        txtName = new JTextField(18);
        addPanel.add(txtName);

        JButton btnAdd = new JButton("Add Student");
        btnAdd.addActionListener(e -> addStudent());
        JButton btnRemove = new JButton("Remove Selected");
        btnRemove.addActionListener(e -> removeSelectedStudent());

        addPanel.add(btnAdd);
        addPanel.add(btnRemove);

        // Attendance control
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        datePanel.setBorder(BorderFactory.createTitledBorder("Attendance"));

        datePanel.add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField(10);
        txtDate.setText(LocalDate.now().toString());
        datePanel.add(txtDate);

        JButton btnLoadForDate = new JButton("Load Attendance For Date");
        btnLoadForDate.addActionListener(e -> loadAttendanceForDate());
        JButton btnMarkPresent = new JButton("Mark Present");
        btnMarkPresent.addActionListener(e -> markSelectedAttendance("P"));
        JButton btnMarkAbsent = new JButton("Mark Absent");
        btnMarkAbsent.addActionListener(e -> markSelectedAttendance("A"));

        datePanel.add(btnLoadForDate);
        datePanel.add(btnMarkPresent);
        datePanel.add(btnMarkAbsent);

        top.add(addPanel);
        top.add(datePanel);

        add(top, BorderLayout.NORTH);

        // Center - tables
        studentModel = new DefaultTableModel(new String[]{"ID","Name","Attendance %"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStudents = new JTable(studentModel);
        tblStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        attendanceModel = new DefaultTableModel(new String[]{"ID","Name","Status (P/A)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAttendance = new JTable(attendanceModel);
        tblAttendance.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tblStudents), new JScrollPane(tblAttendance));
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);

        // Bottom panel - actions and totals
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        lblPercentage = new JLabel("Select a student to see details");
        bottom.add(lblPercentage);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addStudent() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both ID and name", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (Student s : students) {
            if (s.getId().equals(id)) {
                JOptionPane.showMessageDialog(this, "ID already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        Student s = new Student(id, name);
        students.add(s);
        refreshStudentTable();
        txtId.setText(""); txtName.setText("");
    }

    private void removeSelectedStudent() {
        int r = tblStudents.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to remove", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = studentModel.getValueAt(r, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Remove student " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            students.removeIf(st -> st.getId().equals(id));
            // also remove attendance records for that student
            try {
                List<String[]> all = FileStorage.loadAttendanceAll();
                List<String[]> keep = new ArrayList<>();
                for (String[] rec : all) if (!rec[1].equals(id)) keep.add(rec);
                FileStorage.saveAttendance(keep);
            } catch (Exception ex) {
                // ignore if file absent
            }
            refreshStudentTable();
            loadAttendanceForDate();
        }
    }

    private void refreshStudentTable() {
        studentModel.setRowCount(0);
        for (Student s : students) {
            double pct = computeAttendancePercentage(s.getId());
            studentModel.addRow(new Object[]{s.getId(), s.getName(), String.format("%.2f", pct)});
        }
    }

    private double computeAttendancePercentage(String studentId) {
        try {
            List<String[]> all = FileStorage.loadAttendanceAll();
            int present = 0, total = 0;
            Set<String> dates = new HashSet<>();
            for (String[] r : all) {
                if (r[1].equals(studentId)) {
                    total++;
                    if (r[2].equalsIgnoreCase("P")) present++;
                }
            }
            if (total == 0) return 0.0;
            return (present * 100.0) / total;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private void loadInitialData() {
        try {
            students = FileStorage.loadStudents();
        } catch (Exception ex) {
            students = new ArrayList<>();
        }
        refreshStudentTable();
        loadAttendanceForDate();
    }

    private void loadAttendanceForDate() {
        String date = txtDate.getText().trim();
        if (!isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Enter date in YYYY-MM-DD", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        attendanceModel.setRowCount(0);
        try {
            // build a map of studentId -> status for that date
            Map<String, String> map = new HashMap<>();
            List<String[]> recs = FileStorage.getAttendanceForDate(date);
            for (String[] r : recs) map.put(r[1], r[2]);

            // show every student with P/A or blank
            for (Student s : students) {
                String st = map.getOrDefault(s.getId(), "");
                attendanceModel.addRow(new Object[]{s.getId(), s.getName(), st});
            }
        } catch (Exception ex) {
            // no file - just show students empty
            for (Student s : students) {
                attendanceModel.addRow(new Object[]{s.getId(), s.getName(), ""});
            }
        }
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void markSelectedAttendance(String status) {
        int[] rows = tblAttendance.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select rows in attendance table to mark", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String date = txtDate.getText().trim();
        if (!isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Enter valid date YYYY-MM-DD", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // load all existing attendance records
        List<String[]> all;
        try { all = FileStorage.loadAttendanceAll(); } catch (Exception ex) { all = new ArrayList<>(); }

        // convert to map for easy replace, key = date+id
        Map<String, String> map = new HashMap<>();
        for (String[] r : all) map.put(r[0] + "|" + r[1], r[2]);

        for (int r : rows) {
            String id = attendanceModel.getValueAt(r, 0).toString();
            map.put(date + "|" + id, status);
        }

        // convert map back to list
        List<String[]> newAll = new ArrayList<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String[] parts = e.getKey().split("\\|");
            newAll.add(new String[]{parts[0], parts[1], e.getValue()});
        }

        // ensure every student has an entry for the date (if absent - blank stays blank)
        // but we will keep only what map has
        try {
            FileStorage.saveAttendance(newAll);
            JOptionPane.showMessageDialog(this, "Attendance updated and saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            loadAttendanceForDate();
            refreshStudentTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAll() {
        try {
            FileStorage.saveStudents(students);
            // attendance already saved when marked; but save current attendance table snapshot too
            // build list by loading existing and merging current date entries
            List<String[]> all = FileStorage.loadAttendanceAll();
            Map<String, String> map = new HashMap<>();
            for (String[] r : all) map.put(r[0] + "|" + r[1], r[2]);

            // also gather current table rows for date in txtDate
            String date = txtDate.getText().trim();
            if (isValidDate(date)) {
                for (int i = 0; i < attendanceModel.getRowCount(); i++) {
                    String id = String.valueOf(attendanceModel.getValueAt(i, 0));
                    String st = String.valueOf(attendanceModel.getValueAt(i, 2));
                    if (st != null && !st.trim().isEmpty()) map.put(date + "|" + id, st);
                }
            }

            List<String[]> out = new ArrayList<>();
            for (Map.Entry<String,String> e : map.entrySet()) {
                String[] p = e.getKey().split("\\|");
                out.add(new String[]{p[0], p[1], e.getValue()});
            }
            FileStorage.saveAttendance(out);
            JOptionPane.showMessageDialog(this, "Students and attendance saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAttendanceForDate() {
        String date = JOptionPane.showInputDialog(this, "Enter date to export (YYYY-MM-DD):", LocalDate.now().toString());
        if (date == null) return;
        if (!isValidDate(date)) {
            JOptionPane.showMessageDialog(this, "Invalid date format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            List<String[]> recs = FileStorage.getAttendanceForDate(date);
            File out = new File("attendance_export_" + date + ".csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                pw.println("StudentID,Name,Status");
                Map<String, String> map = new HashMap<>();
                for (String[] r : recs) map.put(r[1], r[2]);
                for (Student s : students) {
                    pw.println(s.getId() + "," + s.getName() + "," + map.getOrDefault(s.getId(), ""));
                }
            }
            JOptionPane.showMessageDialog(this, "Exported to " + out.getName(), "Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

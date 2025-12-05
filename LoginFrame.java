import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    private final String VALID_USER = "admin";
    private final String VALID_PASS = "12345";

    public LoginFrame() {
        setTitle("Attendance System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        init();
        setVisible(true);
    }

    private void init() {
        JPanel panel = new JPanel(new GridLayout(3,2,10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        panel.add(new JLabel("Username:"));
        txtUser = new JTextField();
        panel.add(txtUser);

        panel.add(new JLabel("Password:"));
        txtPass = new JPasswordField();
        panel.add(txtPass);

        JButton btnLogin = new JButton("Login");
        JButton btnExit = new JButton("Exit");

        btnLogin.addActionListener(e -> doLogin());
        btnExit.addActionListener(e -> System.exit(0));

        panel.add(btnLogin);
        panel.add(btnExit);

        // Enter on password triggers login
        txtPass.addActionListener(e -> doLogin());

        add(panel);
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword()).trim();
        if (u.equals(VALID_USER) && p.equals(VALID_PASS)) {
            JOptionPane.showMessageDialog(this, "Login successful", "Success", JOptionPane.INFORMATION_MESSAGE);
            new AttendanceApp(u);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

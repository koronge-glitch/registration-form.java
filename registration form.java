import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class RegistrationForm extends JFrame {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/registrationdb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root"; // change to your password

   
    private JTextField nameField;
    private JTextField mobileField;
    private JRadioButton maleRadio, femaleRadio;
    private JComboBox<Integer> dayBox;
    private JComboBox<String> monthBox;
    private JComboBox<Integer> yearBox;
    private JTextArea addressArea;
    private JCheckBox termsBox;
    private JButton saveBtn, clearBtn, refreshBtn;
    private JTable table;
    private DefaultTableModel tableModel;

    public RegistrationForm() {
        super("Registration Form");
        initComponents();
        createLayout();
        addListeners();
        loadRecords();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        nameField = new JTextField(20);
        mobileField = new JTextField(15);

        maleRadio = new JRadioButton("Male", true);
        femaleRadio = new JRadioButton("Female");
        ButtonGroup g = new ButtonGroup();
        g.add(maleRadio);
        g.add(femaleRadio);

        dayBox = new JComboBox<>();
        for (int d = 1; d <= 31; d++) dayBox.addItem(d);

        monthBox = new JComboBox<>(new String[]{
                "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
        });

        yearBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y >= 1900; y--) yearBox.addItem(y);

        addressArea = new JTextArea(4, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        termsBox = new JCheckBox("Accept Terms And Conditions.");

        saveBtn = new JButton("Save");
        clearBtn = new JButton("Clear");
        refreshBtn = new JButton("Refresh");

        
        String[] columns = {"ID","Name","Mobile","Gender","DOB","Address","Accepted","Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void createLayout() {
        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        // Title
        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        JLabel title = new JLabel("Registration Form");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        left.add(title, c);
        row++; c.gridwidth = 1;

        // Name
        c.gridx = 0; c.gridy = row;
        left.add(new JLabel("Name"), c);
        c.gridx = 1;
        left.add(nameField, c);
        row++;

        // Mobile
        c.gridx = 0; c.gridy = row;
        left.add(new JLabel("Mobile"), c);
        c.gridx = 1;
        left.add(mobileField, c);
        row++;

        // Gender
        c.gridx = 0; c.gridy = row;
        left.add(new JLabel("Gender"), c);
        c.gridx = 1;
        JPanel gp = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        gp.add(maleRadio); gp.add(femaleRadio);
        left.add(gp, c);
        row++;

        // DOB
        c.gridx = 0; c.gridy = row;
        left.add(new JLabel("DOB"), c);
        c.gridx = 1;
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dobPanel.add(dayBox); dobPanel.add(monthBox); dobPanel.add(yearBox);
        left.add(dobPanel, c);
        row++;

        // Address
        c.gridx = 0; c.gridy = row;
        left.add(new JLabel("Address"), c);
        c.gridx = 1;
        left.add(new JScrollPane(addressArea), c);
        row++;

        // Terms
        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        left.add(termsBox, c);
        row++; c.gridwidth = 1;

        // Buttons
        c.gridx = 0; c.gridy = row;
        left.add(saveBtn, c);
        c.gridx = 1;
        JPanel bpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bpanel.add(clearBtn); bpanel.add(refreshBtn);
        left.add(bpanel, c);

        // Right: table
        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setBorder(BorderFactory.createTitledBorder("Saved Registrations"));
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        // Split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(420);
        getContentPane().add(split, BorderLayout.CENTER);
    }

    private void addListeners() {
        saveBtn.addActionListener(e -> onSave());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> loadRecords());

        // double-click table row to load into form (optional)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) loadRowIntoForm(r);
                }
            }
        });
    }

    private void onSave() {
        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String gender = maleRadio.isSelected() ? "Male" : "Female";

        int day = (Integer) dayBox.getSelectedItem();
        int month = monthBox.getSelectedIndex() + 1; // 1-12
        int year = (Integer) yearBox.getSelectedItem();
        String dobStr = String.format("%04d-%02d-%02d", year, month, day);

        String address = addressArea.getText().trim();
        boolean accepted = termsBox.isSelected();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!accepted) {
            JOptionPane.showMessageDialog(this, "You must accept the terms.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Insert into database
        String sql = "INSERT INTO registrants (name, mobile, gender, dob, address, accepted_terms) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, mobile.isEmpty() ? null : mobile);
            ps.setString(3, gender);
            ps.setDate(4, Date.valueOf(dobStr));
            ps.setString(5, address.isEmpty() ? null : address);
            ps.setBoolean(6, accepted);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Saved successfully!");
            clearForm();
            loadRecords();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("");
        mobileField.setText("");
        maleRadio.setSelected(true);
        dayBox.setSelectedIndex(0);
        monthBox.setSelectedIndex(0);
        yearBox.setSelectedIndex(0);
        addressArea.setText("");
        termsBox.setSelected(false);
    }

    private void loadRecords() {
        tableModel.setRowCount(0);
        String sql = "SELECT id, name, mobile, gender, dob, address, accepted_terms, created_at FROM registrants ORDER BY created_at DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("mobile"));
                row.add(rs.getString("gender"));
                Date dob = rs.getDate("dob");
                row.add(dob != null ? dob.toString() : "");
                String address = rs.getString("address");
                if (address != null && address.length() > 50) address = address.substring(0, 50) + "...";
                row.add(address);
                row.add(rs.getBoolean("accepted_terms") ? "Yes" : "No");
                row.add(rs.getTimestamp("created_at").toString());
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Load Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRowIntoForm(int tableRow) {
        if (tableRow < 0) return;
        nameField.setText((String) tableModel.getValueAt(tableRow, 1));
        mobileField.setText((String) tableModel.getValueAt(tableRow, 2));
        String g = (String) tableModel.getValueAt(tableRow, 3);
        if ("Male".equalsIgnoreCase(g)) maleRadio.setSelected(true); else femaleRadio.setSelected(true);
        String dob = (String) tableModel.getValueAt(tableRow, 4);
        if (dob != null && !dob.isEmpty()) {
            String[] parts = dob.split("-");
            if (parts.length == 3) {
                yearBox.setSelectedItem(Integer.parseInt(parts[0]));
                monthBox.setSelectedIndex(Integer.parseInt(parts[1]) - 1);
                dayBox.setSelectedItem(Integer.parseInt(parts[2]));
            }
        }
        addressArea.setText((String) tableModel.getValueAt(tableRow, 5));
        termsBox.setSelected("Yes".equals(tableModel.getValueAt(tableRow, 6)));
    }

    public static void main(String[] args) {
        // Load driver explicitly (optional for modern drivers)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // ignore; will be thrown later if driver not present
            System.err.println("MySQL JDBC Driver not found on classpath.");
        }

        SwingUtilities.invokeLater(() -> {
            RegistrationForm f = new RegistrationForm();
            f.setVisible(true);
        });
    }
}

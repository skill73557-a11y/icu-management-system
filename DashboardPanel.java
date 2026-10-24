import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardPanel extends JPanel {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private JLabel totalPatientsValue, admittedValue, criticalValue, availableBedsValue, waitingListValue, occupancyValue;
    private JProgressBar occupancyBar;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel welcome = new JPanel(new BorderLayout(12, 5));
        welcome.setOpaque(false);
        JPanel wt = new JPanel();
        wt.setOpaque(false);
        wt.setLayout(new BoxLayout(wt, BoxLayout.Y_AXIS));
        wt.add(Theme.sectionTitle("Hospital Command Center"));
        wt.add(Box.createVerticalStrut(5));
        wt.add(Theme.mutedLabel("Real-time snapshot of hospital operations."));
        welcome.add(wt, BorderLayout.WEST);
        
        JButton reg = Theme.createButton("+ Admit New Patient", Theme.BLUE);
        // Linking button to PatientsPanel via reflection or just telling user to click Patients tab
        reg.addActionListener(e -> {
            try {
                // Instantly switch to Patients tab and open dialog
                Container parent = getParent();
                if (parent instanceof JPanel) {
                    JPanel content = (JPanel) parent;
                    CardLayout cl = (CardLayout) content.getLayout();
                    cl.show(content, "1");
                    // Find PatientsPanel (this is a bit hacky but works for this structure)
                    for (Component c : content.getComponents()) {
                        if (c instanceof PatientsPanel) {
                            ((PatientsPanel) c).openRegistrationDialog();
                            break;
                        }
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        welcome.add(reg, BorderLayout.EAST);
        body.add(welcome);
        body.add(Box.createVerticalStrut(16));

        JPanel metrics = new JPanel(new GridLayout(1, 5, 12, 12));
        metrics.setOpaque(false);
        totalPatientsValue = new JLabel("—");
        admittedValue = new JLabel("—");
        criticalValue = new JLabel("—");
        availableBedsValue = new JLabel("—");
        waitingListValue = new JLabel("—");
        
        metrics.add(metricCard("TOTAL PATIENTS", totalPatientsValue, "All records", Theme.BLUE, "P"));
        metrics.add(metricCard("CURRENTLY ADMITTED", admittedValue, "In Hospital", Theme.TEAL, "A"));
        metrics.add(metricCard("HIGH / CRITICAL", criticalValue, "Priority cases", Theme.RED, "!"));
        metrics.add(metricCard("AVAILABLE BEDS", availableBedsValue, "Ready to use", Theme.GREEN, "B"));
        metrics.add(metricCard("WAITING LIST", waitingListValue, "No bed available", Theme.ORANGE, "W"));
        body.add(metrics);
        body.add(Box.createVerticalStrut(16));

        JPanel middle = new JPanel(new GridLayout(1, 2, 14, 0));
        middle.setOpaque(false);
        JPanel occupancy = Theme.cardPanel();
        occupancy.setLayout(new BoxLayout(occupancy, BoxLayout.Y_AXIS));
        occupancy.setBorder(BorderFactory.createEmptyBorder(18, 18, 16, 18));
        occupancy.add(Theme.sectionTitle("ICU Occupancy Rate"));
        occupancy.add(Box.createVerticalStrut(13));
        occupancyValue = new JLabel("Loading...");
        occupancyValue.setFont(new Font("SansSerif", Font.BOLD, 28));
        occupancyValue.setForeground(Theme.TEXT);
        occupancy.add(occupancyValue);
        occupancy.add(Box.createVerticalStrut(10));
        occupancyBar = new JProgressBar(0, 100);
        occupancyBar.setStringPainted(true);
        occupancyBar.setForeground(Theme.TEAL);
        occupancyBar.setBackground(Theme.PANEL_ALT);
        occupancyBar.setBorderPainted(false);
        occupancyBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        occupancy.add(occupancyBar);

        JPanel recent = Theme.cardPanel();
        recent.setLayout(new BorderLayout(0, 10));
        recent.setBorder(BorderFactory.createEmptyBorder(17, 18, 15, 18));
        recent.add(Theme.sectionTitle("Recent Admissions"), BorderLayout.NORTH);
        
        middle.add(occupancy); middle.add(recent);
        middle.setPreferredSize(new Dimension(800, 170));
        middle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        body.add(middle);
        
        add(body, BorderLayout.NORTH);
        loadData();
    }

    private JPanel metricCard(String title, JLabel value, String subtitle, Color accent, String symbol) {
        JPanel card = Theme.cardPanel();
        card.setLayout(new BorderLayout(8, 7));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 15, 15));
        JPanel top = new JPanel(new BorderLayout(7, 0));
        top.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(new Font("SansSerif", Font.BOLD, 10));
        name.setForeground(Theme.MUTED);
        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.BOLD, 15));
        icon.setForeground(Theme.WHITE);
        icon.setOpaque(true);
        icon.setBackground(accent.darker());
        icon.setPreferredSize(new Dimension(32, 32));
        top.add(name, BorderLayout.CENTER); top.add(icon, BorderLayout.EAST);
        value.setFont(new Font("SansSerif", Font.BOLD, 30));
        value.setForeground(Theme.TEXT);
        JPanel lower = new JPanel();
        lower.setOpaque(false);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.add(value); lower.add(Box.createVerticalStrut(5)); lower.add(Theme.mutedLabel(subtitle));
        card.add(top, BorderLayout.NORTH); card.add(lower, BorderLayout.CENTER);
        return card;
    }

    public void loadData() {
        threadPool.submit(() -> {
            try (Connection con = DatabaseManager.getConnection()) {
                int total = count(con, "SELECT COUNT(*) FROM patients");
                int admitted = count(con, "SELECT COUNT(*) FROM patients WHERE status = 'ADMITTED'");
                int critical = count(con, "SELECT COUNT(*) FROM patients WHERE status <> 'DISCHARGED' AND emergency_level IN ('HIGH','CRITICAL')");
                int totalBeds = count(con, "SELECT COUNT(*) FROM icu_beds");
                int available = count(con, "SELECT COUNT(*) FROM icu_beds WHERE occupied = FALSE");
                int waiting = count(con, "SELECT COUNT(*) FROM patients WHERE status = 'ADMITTED' AND bed_id IS NULL");
                int occupied = totalBeds - available;
                int percent = totalBeds == 0 ? 0 : (int)Math.round(occupied * 100.0 / totalBeds);

                SwingUtilities.invokeLater(() -> {
                    totalPatientsValue.setText(String.valueOf(total));
                    admittedValue.setText(String.valueOf(admitted));
                    criticalValue.setText(String.valueOf(critical));
                    availableBedsValue.setText(String.valueOf(available));
                    waitingListValue.setText(String.valueOf(waiting));
                    occupancyValue.setText(percent + "% occupied");
                    occupancyBar.setValue(percent);
                    occupancyBar.setString(percent + "%");
                });
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    private int count(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
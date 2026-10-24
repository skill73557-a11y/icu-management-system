import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ReportsPanel extends JPanel {
    public ReportsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(new BorderLayout()); intro.setOpaque(false);
        intro.add(Theme.sectionTitle("Hospital Analytics"), BorderLayout.WEST);
        JButton refresh = Theme.createButton("Refresh", Theme.BLUE); refresh.addActionListener(e -> loadData());
        intro.add(refresh, BorderLayout.EAST);
        
        add(intro, BorderLayout.NORTH);
        loadData();
    }

    public void loadData() {
        // Simple synchronous load for reports to keep it brief
        removeAll();
        add(new JLabel("Loading...") {{ setForeground(Theme.TEXT); }}, BorderLayout.CENTER);
        
        try (Connection con = DatabaseManager.getConnection()) {
            int all = count(con, "SELECT COUNT(*) FROM patients");
            int admitted = count(con, "SELECT COUNT(*) FROM patients WHERE status='ADMITTED'");
            double rev = 0;
            try(PreparedStatement ps = con.prepareStatement("SELECT SUM(bill_amount) FROM patients WHERE status='DISCHARGED'"); ResultSet rs = ps.executeQuery()) {
                if(rs.next()) rev = rs.getDouble(1);
            }
            
            JPanel overview = new JPanel(new GridLayout(1, 3, 12, 12)); overview.setOpaque(false);
            overview.add(metricCard("TOTAL PATIENTS", String.valueOf(all), Theme.BLUE));
            overview.add(metricCard("ACTIVE ADMISSIONS", String.valueOf(admitted), Theme.TEAL));
            overview.add(metricCard("TOTAL REVENUE", "Rs. " + (int)rev, Theme.GREEN));
            
            JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(overview);
            content.add(Box.createVerticalStrut(20));
            content.add(Theme.mutedLabel("Financial and operational reports."));
            
            add(content, BorderLayout.CENTER);
            revalidate(); repaint();
            
        } catch (SQLException ex) { Theme.showError(this, "Load failed", ex); }
    }

    private JPanel metricCard(String title, String value, Color accent) {
        JPanel card = Theme.cardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel t = new JLabel(title); t.setForeground(Theme.MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("SansSerif", Font.BOLD, 30)); v.setForeground(accent);
        card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.CENTER);
        return card;
    }

    private int count(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
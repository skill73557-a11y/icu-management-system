import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class EmergencyPanel extends JPanel {
    private DefaultTableModel queueModel;

    public EmergencyPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(); intro.setOpaque(false); intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.add(Theme.sectionTitle("Emergency Priority Queue"));
        intro.add(Box.createVerticalStrut(5));
        intro.add(Theme.mutedLabel("Patients sorted by severity."));
        
        queueModel = new DefaultTableModel(new String[]{"Priority","ID","Name","Age","Emergency","Bed","Doctor"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable queueTable = new JTable(queueModel); Theme.styleTable(queueTable);
        
        JScrollPane scroll = new JScrollPane(queueTable); scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JPanel card = Theme.cardPanel(); card.setLayout(new BorderLayout()); card.add(scroll);
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton refresh = Theme.createButton("Refresh Queue", Theme.BLUE); refresh.addActionListener(e -> loadData()); 
        actions.add(refresh);
        
        add(intro, BorderLayout.NORTH); add(card, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        loadData();
    }

    public void loadData() {
        queueModel.setRowCount(0);
        // Explicit Thread for Sir's requirement
        Thread dbThread = new Thread(() -> {
            String sql = "SELECT p.id, p.name, p.age, p.emergency_level, p.bed_id, d.name as doc_name " +
                         "FROM patients p LEFT JOIN doctors d ON p.doctor_id = d.id " +
                         "WHERE p.status <> 'DISCHARGED' " +
                         "ORDER BY CASE UPPER(p.emergency_level) WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END";
            try (Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                int priority = 0;
                while (rs.next()) { 
                    priority++; Object bed = rs.getObject("bed_id");
                    final int p = priority; 
                    int id = rs.getInt("id"); 
                    String nm = rs.getString("name");
                    int ag = rs.getInt("age"); 
                    String el = rs.getString("emergency_level"); 
                    String doc = rs.getString("doc_name");
                    
                    SwingUtilities.invokeLater(() -> queueModel.addRow(new Object[]{p, id, nm, ag, el, bed == null ? "WAITING" : bed, doc == null ? "None" : doc}));
                }
            } catch (SQLException ex) { SwingUtilities.invokeLater(() -> Theme.showError(this, "Queue load failed", ex)); }
        });
        dbThread.start();
    }
}
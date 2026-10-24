import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ICUBedsPanel extends JPanel {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private JPanel bedGrid;
    private JLabel bedSummary;

    public ICUBedsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(new BorderLayout()); intro.setOpaque(false);
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Theme.sectionTitle("ICU Bed Management")); text.add(Box.createVerticalStrut(5));
        text.add(Theme.mutedLabel("Live bed availability. Automatically allocates beds to waiting patients."));
        bedSummary = new JLabel("Loading..."); bedSummary.setForeground(Theme.BLUE);
        bedSummary.setFont(new Font("SansSerif", Font.BOLD, 13));
        intro.add(text, BorderLayout.WEST); intro.add(bedSummary, BorderLayout.EAST);
        
        bedGrid = new JPanel(new GridLayout(0, 3, 15, 15)); bedGrid.setOpaque(false);
        JScrollPane scroll = new JScrollPane(bedGrid); scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);
        
        add(intro, BorderLayout.NORTH); add(scroll, BorderLayout.CENTER);
        loadData();
    }

    public void loadData() {
        bedGrid.removeAll();
        threadPool.submit(() -> {
            try (Connection con = DatabaseManager.getConnection()) {
                int freeBeds = getCount(con, "SELECT COUNT(*) FROM icu_beds WHERE occupied = FALSE");
                int waitingPatients = getCount(con, "SELECT COUNT(*) FROM patients WHERE status='ADMITTED' AND bed_id IS NULL");

                // SMART ALLOCATION: If there are free beds and waiting patients, auto-assign!
                if (freeBeds > 0 && waitingPatients > 0) {
                    for (int i = 0; i < Math.min(freeBeds, waitingPatients); i++) {
                        int bedId = -1;
                        String patientName = "";
                        int patientId = -1;

                        // 1. Get one free bed
                        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM icu_beds WHERE occupied=FALSE LIMIT 1");
                             ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) bedId = rs.getInt("id");
                        }
                        
                        // 2. Get highest priority waiting patient
                        if (bedId != -1) {
                            try (PreparedStatement ps = con.prepareStatement(
                                    "SELECT id, name FROM patients WHERE status='ADMITTED' AND bed_id IS NULL " +
                                    "ORDER BY CASE UPPER(emergency_level) WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END LIMIT 1");
                                 ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    patientId = rs.getInt("id");
                                    patientName = rs.getString("name");
                                }
                            }
                        }
                        
                        // 3. Assign the bed
                        if (bedId != -1 && patientId != -1) {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE patients SET bed_id=? WHERE id=?")) {
                                ps.setInt(1, bedId); ps.setInt(2, patientId); ps.executeUpdate();
                            }
                            try (PreparedStatement ps = con.prepareStatement("UPDATE icu_beds SET occupied=TRUE WHERE id=?")) {
                                ps.setInt(1, bedId); ps.executeUpdate();
                            }
                            // Show popup to user
                            final int finalBed = bedId;
                            final String finalName = patientName;
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, 
                                "🟢 AUTOMATIC ALLOCATION:\nBed " + finalBed + " was just assigned to waiting patient: " + finalName));
                        }
                    }
                }

                // Draw the UI
                String sql = "SELECT b.id, b.occupied, p.name, p.emergency_level FROM icu_beds b " +
                             "LEFT JOIN patients p ON p.bed_id = b.id AND p.status <> 'DISCHARGED' ORDER BY b.id";
                int total = 0, occupied = 0;
                
                try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        total++; 
                        boolean used = rs.getBoolean("occupied"); 
                        if (used) occupied++;
                        
                        int id = rs.getInt("id"); 
                        String patient = rs.getString("name");
                        String emergency = rs.getString("emergency_level");
                        
                        // --- COLORFUL BED LOGIC ---
                        Color borderColor = Theme.GREEN; 
                        Color bedBgColor = Theme.PANEL;  
                        String statusText = used ? "OCCUPIED" : "AVAILABLE";
                        
                        if (used && emergency != null) {
                            if (emergency.equalsIgnoreCase("CRITICAL")) {
                                borderColor = new Color(200, 50, 50); // Red
                                bedBgColor = new Color(255, 235, 235); // Light red bg
                            } else if (emergency.equalsIgnoreCase("HIGH")) {
                                borderColor = new Color(220, 130, 40); // Orange
                                bedBgColor = new Color(255, 245, 230); // Light orange bg
                            } else if (emergency.equalsIgnoreCase("MEDIUM")) {
                                borderColor = new Color(180, 180, 50); // Yellow/Olive
                                bedBgColor = new Color(250, 250, 230); // Light yellow bg
                            } else {
                                borderColor = new Color(60, 130, 80); // Green (LOW)
                                bedBgColor = new Color(235, 245, 235); // Light green bg
                            }
                        } else if (!used) {
                            borderColor = new Color(40, 160, 120); // Mint (AVAILABLE)
                            bedBgColor = new Color(240, 250, 245); // Light mint bg
                        }

                        JPanel bed = new JPanel(new BorderLayout(10,5));
                        bed.setBackground(bedBgColor);
                        bed.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 5, 0, 0, borderColor), 
                            new EmptyBorder(14,15,14,14)
                        ));
                        
                        JPanel top = new JPanel(new BorderLayout()); 
                        top.setOpaque(false);
                        
                        JLabel name = new JLabel("Bed " + id); 
                        name.setFont(new Font("SansSerif", Font.BOLD, 16)); 
                        name.setForeground(Theme.NAVY); 
                        
                        JLabel status = new JLabel(statusText);
                        status.setOpaque(true); 
                        status.setBackground(borderColor);
                        status.setForeground(Color.WHITE);
                        status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                        
                        top.add(name, BorderLayout.WEST); 
                        top.add(status, BorderLayout.EAST);
                        
                        JLabel patientLabel = new JLabel(used ? "Patient: " + (patient != null ? patient : "Unknown") : "Ready for allocation");
                        patientLabel.setForeground(Theme.TEXT);
                        
                        bed.add(top, BorderLayout.NORTH);
                        bed.add(patientLabel, BorderLayout.CENTER);
                        
                        SwingUtilities.invokeLater(() -> bedGrid.add(bed));
                    }
                }
                
                final int fTotal = total, fOccupied = occupied, fAvail = total - occupied;
                SwingUtilities.invokeLater(() -> { 
                    bedSummary.setText(fAvail + " available / " + fOccupied + " occupied / " + fTotal + " total");
                    bedGrid.revalidate(); 
                    bedGrid.repaint(); 
                });
                
            } catch (SQLException ex) { 
                SwingUtilities.invokeLater(() -> Theme.showError(this, "Load failed", ex)); 
            }
        });
    }

    private int getCount(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
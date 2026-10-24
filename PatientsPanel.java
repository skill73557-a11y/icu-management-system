import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientsPanel extends JPanel {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private DefaultTableModel patientModel;
    private JTable patientTable;
    private JTextField searchField;

    public PatientsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(new BorderLayout());
        intro.setOpaque(false);
        JPanel t = new JPanel();
        t.setOpaque(false); t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Theme.sectionTitle("Patient Directory"));
        t.add(Box.createVerticalStrut(5));
        t.add(Theme.mutedLabel("Search, review, register and discharge patients."));
        intro.add(t, BorderLayout.WEST);
        
        JButton add = Theme.createButton("+ Register New Patient", Theme.BLUE);
        add.addActionListener(e -> openRegistrationDialog());
        intro.add(add, BorderLayout.EAST);

        JPanel search = new JPanel(new BorderLayout(10, 0));
        search.setBackground(Theme.PANEL);
        search.setBorder(new EmptyBorder(12, 14, 12, 14));
        searchField = new JTextField();
        Theme.styleTextField(searchField);
        searchField.addActionListener(e -> loadData());
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        buttons.setOpaque(false);
        JButton find = Theme.createButton("Search", Theme.BLUE); find.addActionListener(e -> loadData());
        JButton clear = Theme.createButton("Clear", Theme.NAVY_LIGHT);
        clear.addActionListener(e -> { searchField.setText(""); loadData(); });
        buttons.add(find); buttons.add(clear);
        search.add(searchField, BorderLayout.CENTER); search.add(buttons, BorderLayout.EAST);

        patientModel = new DefaultTableModel(new String[]{"ID","Name","Age","Gender","Emergency","Bed","Doctor","Status","Admitted"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        patientTable = new JTable(patientModel);
        Theme.styleTable(patientTable);
        
        JScrollPane tableScroll = new JScrollPane(patientTable);
        tableScroll.getViewport().setBackground(Theme.PANEL);
        JPanel tableCard = Theme.cardPanel(); tableCard.setLayout(new BorderLayout()); tableCard.add(tableScroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8)); actions.setOpaque(false);
        JButton refresh = Theme.createButton("Refresh", Theme.NAVY_LIGHT); refresh.addActionListener(e -> loadData());
        JButton discharge = Theme.createButton("Discharge & Bill", Theme.RED); discharge.addActionListener(e -> dischargePatient());
        actions.add(refresh); actions.add(discharge);
        
        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(intro); top.add(Box.createVerticalStrut(15)); top.add(search);
        add(top, BorderLayout.NORTH); add(tableCard, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        
        loadData();
    }

    public void loadData() {
        patientModel.setRowCount(0);
        String filter = searchField.getText().trim();
        threadPool.submit(() -> {
            String sql = "SELECT p.id, p.name, p.age, p.gender, p.emergency_level, p.bed_id, d.name as doc_name, p.status, p.admitted_at " +
                         "FROM patients p LEFT JOIN doctors d ON p.doctor_id = d.id " +
                         "WHERE CAST(p.id AS CHAR) LIKE ? OR p.name LIKE ? ORDER BY p.id DESC";
            try (Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "%" + filter + "%"); ps.setString(2, "%" + filter + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object bed = rs.getObject("bed_id");
                        int id = rs.getInt("id"); String nm = rs.getString("name"); int ag = rs.getInt("age");
                        String gd = rs.getString("gender"); String el = rs.getString("emergency_level");
                        String doc = rs.getString("doc_name"); String st = rs.getString("status");
                        Timestamp ad = rs.getTimestamp("admitted_at");
                        SwingUtilities.invokeLater(() -> patientModel.addRow(new Object[]{
                            id, nm, ag, gd, el, (bed == null && st.equals("DISCHARGED")) ? "N/A" : (bed == null ? "WAITING" : bed), doc == null ? "None" : doc, st, ad
                        }));
                    }
                }
            } catch (SQLException ex) { SwingUtilities.invokeLater(() -> Theme.showError(this, "Load failed", ex)); }
        });
    }

    public void openRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Admit Patient", true);
        dialog.setSize(520, 520); dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG); dialog.setLayout(new BorderLayout(10,10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL); form.setBorder(new EmptyBorder(22,25,22,25));
        
        JTextField name = new JTextField(20), age = new JTextField(20);
        Theme.styleTextField(name); Theme.styleTextField(age);
        JComboBox<String> gender = new JComboBox<>(new String[]{"Male","Female","Other"}); Theme.styleCombo(gender);
        JComboBox<String> emergency = new JComboBox<>(new String[]{"LOW","MEDIUM","HIGH","CRITICAL"}); Theme.styleCombo(emergency);
        JComboBox<String> doctorCombo = new JComboBox<>(); Theme.styleCombo(doctorCombo);
        
        try (Connection con = DatabaseManager.getConnection(); 
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM doctors WHERE status='ON_DUTY'");
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) doctorCombo.addItem(rs.getInt("id") + " - " + rs.getString("name"));
        } catch(SQLException ex) { ex.printStackTrace(); }

        JTextArea symptoms = new JTextArea(3,20);
        symptoms.setBackground(Theme.PANEL_ALT); symptoms.setForeground(Theme.TEXT);
        
        addFormRow(form,0,"Patient Name",name); addFormRow(form,1,"Age",age);
        addFormRow(form,2,"Gender",gender); addFormRow(form,3,"Emergency Level",emergency);
        addFormRow(form,4,"Assign Doctor",doctorCombo); addFormRow(form,5,"Symptoms",new JScrollPane(symptoms));
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); actions.setBackground(Theme.BG);
        JButton cancel = Theme.createButton("Cancel",Theme.NAVY_LIGHT); cancel.addActionListener(e -> dialog.dispose());
        JButton save = Theme.createButton("Admit",Theme.BLUE);
        
        save.addActionListener(e -> {
            String patientName=name.getText().trim(), ageText=age.getText().trim(), symptomText=symptoms.getText().trim();
            if (patientName.isEmpty() || ageText.isEmpty()) { JOptionPane.showMessageDialog(dialog,"Name and Age required."); return; }
                        int patientAge;
            try {
                patientAge = Integer.parseInt(ageText);
                if(patientAge < 0 || patientAge > 130) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number for age.");
                return;
            }
            int docId = doctorCombo.getSelectedItem() != null ? Integer.parseInt(doctorCombo.getSelectedItem().toString().split(" - ")[0]) : -1;
            
            insertPatient(patientName, patientAge, (String)gender.getSelectedItem(), (String)emergency.getSelectedItem(), symptomText, docId);
            dialog.dispose();
        });
        actions.add(cancel); actions.add(save);
        dialog.add(form, BorderLayout.CENTER); dialog.add(actions, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    private void insertPatient(String name, int age, String gender, String emergency, String symptoms, int docId) {
        threadPool.submit(() -> {
            try (Connection con = DatabaseManager.getConnection()) {
                con.setAutoCommit(false);
                try {
                    Integer bedId = null;
                    try (PreparedStatement ps = con.prepareStatement("SELECT id FROM icu_beds WHERE occupied = FALSE LIMIT 1 FOR UPDATE");
                         ResultSet rs = ps.executeQuery()) { if(rs.next()) bedId = rs.getInt("id"); }
                    
                    if (bedId != null) {
                        try (PreparedStatement ps = con.prepareStatement("UPDATE icu_beds SET occupied=TRUE WHERE id=?")) {
                            ps.setInt(1, bedId); ps.executeUpdate();
                        }
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO patients (name,age,gender,emergency_level,symptoms,bed_id,doctor_id) VALUES (?,?,?,?,?,?,?)")) {
                        ps.setString(1,name); ps.setInt(2,age); ps.setString(3,gender);
                        ps.setString(4,emergency); ps.setString(5,symptoms);
                        if(bedId==null) ps.setNull(6,Types.INTEGER); else ps.setInt(6,bedId);
                        if(docId==-1) ps.setNull(7,Types.INTEGER); else ps.setInt(7, docId);
                        ps.executeUpdate();
                    }
                    con.commit();
                    final Integer finalBedId = bedId;
                    SwingUtilities.invokeLater(() -> {
                        String msg = finalBedId == null ? "No beds available! Patient on WAITING LIST." : "Admitted. Bed " + finalBedId + " assigned.";
                        JOptionPane.showMessageDialog(this, msg);
                        loadData();
                    });
                } catch(SQLException ex) { con.rollback(); throw ex; }
            } catch(SQLException ex) { SwingUtilities.invokeLater(() -> Theme.showError(this, "Registration failed", ex)); }
        });
    }

    private void dischargePatient() {
        int selected = patientTable.getSelectedRow();
        if(selected < 0) { JOptionPane.showMessageDialog(this,"Select a patient first."); return; }
        int row = patientTable.convertRowIndexToModel(selected);
        int id = ((Number)patientModel.getValueAt(row,0)).intValue();
        String name = (String)patientModel.getValueAt(row,1);
        String status = (String)patientModel.getValueAt(row,7);
        
        if ("DISCHARGED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This patient is already discharged.", "Already Discharged", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double bill = 0; Integer bedId = null;
        try(Connection con = DatabaseManager.getConnection()) {
            try(PreparedStatement ps = con.prepareStatement("SELECT bed_id, TIMESTAMPDIFF(DAY, admitted_at, NOW()) as days FROM patients WHERE id=?")) {
                ps.setInt(1,id);
                try(ResultSet rs = ps.executeQuery()) {
                    if(rs.next()) {
                        Object b = rs.getObject("bed_id"); if(b!=null) bedId = ((Number)b).intValue();
                        int days = rs.getInt("days"); if(days < 1) days = 1; bill = days * 2000.0;
                    }
                }
            }
        } catch(SQLException ex) { Theme.showError(this, "Bill error", ex); return; }

        if(JOptionPane.showConfirmDialog(this, "Discharge " + name + "?\nTotal Bill: Rs. " + bill, "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        
        try(Connection con = DatabaseManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Mark patient as discharged
                try(PreparedStatement ps = con.prepareStatement("UPDATE patients SET status='DISCHARGED', bed_id=NULL, bill_amount=?, discharged_at=NOW() WHERE id=?")) {
                    ps.setDouble(1, bill); ps.setInt(2, id); ps.executeUpdate();
                }
                
                String waitingPatientName = null;
                
                // 2. Free the bed and auto-assign if someone is waiting
                if(bedId != null) {
                    try(PreparedStatement ps = con.prepareStatement("UPDATE icu_beds SET occupied=FALSE WHERE id=?")) {
                        ps.setInt(1, bedId); ps.executeUpdate();
                    }
                    
                    // 3. Find highest priority waiting patient
                    String getWaitSql = "SELECT p.id, p.name FROM patients p WHERE p.status='ADMITTED' AND p.bed_id IS NULL " +
                                        "ORDER BY CASE UPPER(p.emergency_level) WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END LIMIT 1";
                    try(PreparedStatement ps = con.prepareStatement(getWaitSql); ResultSet rs = ps.executeQuery()) {
                        if(rs.next()) {
                            int waitId = rs.getInt("id");
                            waitingPatientName = rs.getString("name");
                            
                            // 4. Assign bed to waiting patient
                            try(PreparedStatement ps2 = con.prepareStatement("UPDATE patients SET bed_id=? WHERE id=?")) {
                                ps2.setInt(1, bedId); ps2.setInt(2, waitId); ps2.executeUpdate();
                            }
                            // 5. Mark bed as occupied again
                            try(PreparedStatement ps3 = con.prepareStatement("UPDATE icu_beds SET occupied=TRUE WHERE id=?")) {
                                ps3.setInt(1, bedId); ps3.executeUpdate();
                            }
                        }
                    }
                }
                con.commit(); 
                
                final String finalWaitingName = waitingPatientName;
                final Integer finalBedId = bedId;
                final int finalBill = (int) bill; // FIXED: Final variable for lambda
                
                SwingUtilities.invokeLater(() -> { 
                    String msg = "Patient discharged. Bill generated: Rs. " + finalBill;
                    if (finalWaitingName != null) {
                        msg += "\n\n🟢 AUTOMATIC ALLOCATION:\nBed " + finalBedId + " was immediately assigned to waiting patient: " + finalWaitingName;
                    }
                    JOptionPane.showMessageDialog(this, msg, "Discharge Successful", JOptionPane.INFORMATION_MESSAGE); 
                    loadData(); 
                });
                
            } catch(SQLException ex) { con.rollback(); Theme.showError(this, "Discharge failed", ex); }
        } catch(SQLException ex) { Theme.showError(this, "DB error", ex); }
    }

    private void addFormRow(JPanel p,int row,String lbl,JComponent f) {
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7,5,7,5); g.anchor = GridBagConstraints.WEST;
        g.gridx=0; g.gridy=row; JLabel l = new JLabel(lbl); l.setForeground(Theme.TEXT); p.add(l,g);
        g.gridx=1; g.weightx=1; g.fill=GridBagConstraints.HORIZONTAL; p.add(f,g);
    }
}
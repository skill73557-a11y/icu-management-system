import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DoctorsPanel extends JPanel {
    private DefaultTableModel doctorModel;

    public DoctorsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(new BorderLayout()); intro.setOpaque(false);
        JPanel t = new JPanel(); t.setOpaque(false); t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Theme.sectionTitle("Doctor Directory"));
        t.add(Box.createVerticalStrut(5));
        t.add(Theme.mutedLabel("Manage staff and duty status."));
        intro.add(t, BorderLayout.WEST);
        
        JButton add = Theme.createButton("+ Add Doctor", Theme.BLUE);
        add.addActionListener(e -> openDialog());
        intro.add(add, BorderLayout.EAST);

        doctorModel = new DefaultTableModel(new String[]{"ID","Name","Specialty","Status"},0);
        JTable table = new JTable(doctorModel); Theme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JPanel card = Theme.cardPanel(); card.setLayout(new BorderLayout()); card.add(scroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton toggle = Theme.createButton("Toggle Duty Status", Theme.ORANGE); 
        toggle.addActionListener(e -> toggleStatus(table)); 
        actions.add(toggle);
        
        add(intro, BorderLayout.NORTH); add(card, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        loadData();
    }

    public void loadData() {
        doctorModel.setRowCount(0);
        try (Connection con = DatabaseManager.getConnection(); 
             PreparedStatement ps = con.prepareStatement("SELECT * FROM doctors");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctorModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("specialty"), rs.getString("status")});
            }
        } catch (SQLException ex) { Theme.showError(this, "Load failed", ex); }
    }

    private void openDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Doctor", true);
        d.setSize(400, 250); d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Theme.BG); d.setLayout(new GridLayout(3, 2, 10, 10));
        
        JTextField name = new JTextField(); Theme.styleTextField(name);
        JTextField spec = new JTextField(); Theme.styleTextField(spec);
        d.add(new JLabel("Name:")).setForeground(Theme.TEXT);
        d.add(name);
        d.add(new JLabel("Specialty:")).setForeground(Theme.TEXT);
        d.add(spec);
        
        JButton save = Theme.createButton("Save", Theme.BLUE);
        save.addActionListener(e -> {
            try(Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO doctors (name, specialty) VALUES (?,?)")) {
                ps.setString(1, name.getText()); ps.setString(2, spec.getText()); ps.executeUpdate();
                d.dispose(); loadData();
            } catch(SQLException ex) { Theme.showError(this, "Error", ex); }
        });
        d.add(save); d.setVisible(true);
    }

    private void toggleStatus(JTable table) {
        int row = table.getSelectedRow();
        if(row < 0) return;
        int id = (int)doctorModel.getValueAt(row, 0);
        String current = (String)doctorModel.getValueAt(row, 3);
        String newStatus = current.equals("ON_DUTY") ? "OFF_DUTY" : "ON_DUTY";
        try(Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE doctors SET status=? WHERE id=?")) {
            ps.setString(1, newStatus); ps.setInt(2, id); ps.executeUpdate(); loadData();
        } catch(SQLException ex) { Theme.showError(this, "Error", ex); }
    }
}
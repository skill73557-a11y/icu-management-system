import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageDataPanel extends JPanel {
    private DefaultTableModel historyModel;
    private JTable historyTable;

    public ManageDataPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(new BorderLayout()); 
        intro.setOpaque(false);
        JPanel t = new JPanel(); 
        t.setOpaque(false); 
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Theme.sectionTitle("Database Management"));
        t.add(Box.createVerticalStrut(5));
        t.add(Theme.mutedLabel("Delete specific records or clear the entire database history."));
        intro.add(t, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Theme.PANEL);
        searchPanel.setBorder(new EmptyBorder(12, 14, 12, 14));
        JTextField searchField = new JTextField();
        Theme.styleTextField(searchField);
        JButton searchBtn = Theme.createButton("Search", Theme.BLUE);
        searchPanel.add(new JLabel("Search Records: ") {{ setForeground(Theme.TEXT); }}, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        historyModel = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Status", "Admitted Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        Theme.styleTable(historyTable);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Theme.PANEL);
        
        JPanel tableCard = Theme.cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(scroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8)); 
        actions.setOpaque(false);
        
        JButton refreshBtn = Theme.createButton("Refresh History", Theme.NAVY_LIGHT);
        JButton deleteBtn = Theme.createButton("Delete Selected Patient", Theme.RED);
        JButton wipeBtn = Theme.createButton("⚠️ Wipe Entire Database", new Color(139, 0, 0)); 
        
        actions.add(refreshBtn);
        actions.add(deleteBtn);
        actions.add(wipeBtn);

        JPanel top = new JPanel(); 
        top.setOpaque(false); 
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(intro); 
        top.add(Box.createVerticalStrut(15)); 
        top.add(searchPanel);
        
        add(top, BorderLayout.NORTH); 
        add(tableCard, BorderLayout.CENTER); 
        add(actions, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadHistory(searchField.getText().trim()));
        searchField.addActionListener(e -> loadHistory(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadHistory(""); });

        deleteBtn.addActionListener(e -> {
            int row = historyTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a patient record to delete."); return; }
            int id = (int) historyModel.getValueAt(row, 0);
            String name = (String) historyModel.getValueAt(row, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Permanently delete patient #" + id + " (" + name + ")?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DatabaseManager.getConnection()) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE icu_beds b JOIN patients p ON b.id = p.bed_id SET b.occupied = FALSE WHERE p.id = ?")) {
                        ps.setInt(1, id); ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM patients WHERE id = ?")) {
                        ps.setInt(1, id); ps.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(this, "Patient record deleted.");
                    loadHistory("");
                } catch (SQLException ex) { Theme.showError(this, "Delete failed", ex); }
            }
        });

        wipeBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "⚠️ DANGER ⚠️\n\nDelete ALL patient records and free ALL beds?", 
                "Wipe Database", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection con = DatabaseManager.getConnection()) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate("DELETE FROM patients");
                    stmt.executeUpdate("UPDATE icu_beds SET occupied = FALSE");
                    JOptionPane.showMessageDialog(this, "Database wiped clean.");
                    loadHistory("");
                } catch (SQLException ex) { Theme.showError(this, "Wipe failed", ex); }
            }
        });

        loadHistory("");
    }

    public void loadData() {
        loadHistory("");
    }

    public void loadHistory(String filter) {
        historyModel.setRowCount(0);
        String sql = "SELECT id, name, age, status, admitted_at FROM patients " +
                     "WHERE CAST(id AS CHAR) LIKE ? OR name LIKE ? ORDER BY id DESC";
                     
        try (Connection con = DatabaseManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + filter + "%"); ps.setString(2, "%" + filter + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historyModel.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getString("name"), 
                        rs.getInt("age"), 
                        rs.getString("status"), 
                        rs.getTimestamp("admitted_at")
                    });
                }
            }
        } catch (SQLException ex) { Theme.showError(this, "Load failed", ex); }
    }
}
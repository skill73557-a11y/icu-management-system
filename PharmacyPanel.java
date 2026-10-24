import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PharmacyPanel extends JPanel {
    private DefaultTableModel pharmacyModel;

    public PharmacyPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel intro = new JPanel(new BorderLayout()); 
        intro.setOpaque(false);
        
        JPanel t = new JPanel(); 
        t.setOpaque(false); 
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(Theme.sectionTitle("Pharmacy & Inventory"));
        t.add(Box.createVerticalStrut(5));
        t.add(Theme.mutedLabel("Manage medicine stock."));
        intro.add(t, BorderLayout.WEST);
        
        JButton add = Theme.createButton("+ Add Medicine", Theme.TEAL);
        add.addActionListener(e -> openDialog());
        intro.add(add, BorderLayout.EAST);

        pharmacyModel = new DefaultTableModel(new String[]{"ID","Medicine Name","Stock","Price"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(pharmacyModel); 
        Theme.styleTable(table);
        
        JScrollPane scroll = new JScrollPane(table); 
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JPanel card = Theme.cardPanel(); 
        card.setLayout(new BorderLayout()); 
        card.add(scroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        actions.setOpaque(false);
        
        JButton restock = Theme.createButton("Restock Selected", Theme.BLUE); 
        restock.addActionListener(e -> restock(table)); 
        actions.add(restock);
        
        add(intro, BorderLayout.NORTH); 
        add(card, BorderLayout.CENTER); 
        add(actions, BorderLayout.SOUTH);
        loadData();
    }

    public void loadData() {
        pharmacyModel.setRowCount(0);
        try (Connection con = DatabaseManager.getConnection(); 
             PreparedStatement ps = con.prepareStatement("SELECT * FROM pharmacy");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int stock = rs.getInt("stock");
                String display = stock < 50 ? stock + " (LOW)" : String.valueOf(stock);
                pharmacyModel.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("medicine_name"), 
                    display, 
                    rs.getDouble("price_per_unit")
                });
            }
        } catch (SQLException ex) { Theme.showError(this, "Load failed", ex); }
    }

    private void openDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Medicine", true);
        d.setSize(400, 250); 
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Theme.BG); 
        d.setLayout(new GridLayout(4, 2, 10, 10));
        
        JTextField name = new JTextField(); 
        Theme.styleTextField(name);
        JTextField stock = new JTextField(); 
        Theme.styleTextField(stock);
        JTextField price = new JTextField(); 
        Theme.styleTextField(price);
        
        JLabel l1 = new JLabel("Medicine Name:"); l1.setForeground(Theme.TEXT);
        JLabel l2 = new JLabel("Stock:"); l2.setForeground(Theme.TEXT);
        JLabel l3 = new JLabel("Price:"); l3.setForeground(Theme.TEXT);
        
        d.add(l1); d.add(name);
        d.add(l2); d.add(stock);
        d.add(l3); d.add(price);
        
        JButton save = Theme.createButton("Save", Theme.TEAL);
        save.addActionListener(e -> {
            try (Connection con = DatabaseManager.getConnection(); 
                 PreparedStatement ps = con.prepareStatement("INSERT INTO pharmacy (medicine_name, stock, price_per_unit) VALUES (?,?,?)")) {
                ps.setString(1, name.getText()); 
                ps.setInt(2, Integer.parseInt(stock.getText())); 
                ps.setDouble(3, Double.parseDouble(price.getText())); 
                ps.executeUpdate();
                d.dispose(); 
                loadData();
            } catch (Exception ex) { 
                Theme.showError(this, "Error", new SQLException(ex.getMessage())); 
            }
        });
        d.add(save); 
        d.setVisible(true);
    }

    private void restock(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String name = (String) pharmacyModel.getValueAt(row, 1);
        String input = JOptionPane.showInputDialog("Add stock for " + name + ":");
        if (input != null && !input.isEmpty()) {
            try (Connection con = DatabaseManager.getConnection(); 
                 PreparedStatement ps = con.prepareStatement("UPDATE pharmacy SET stock = stock + ? WHERE medicine_name=?")) {
                ps.setInt(1, Integer.parseInt(input)); 
                ps.setString(2, name); 
                ps.executeUpdate(); 
                loadData();
            } catch (Exception ex) { 
                Theme.showError(this, "Error", new SQLException(ex.getMessage())); 
            }
        }
    }
}
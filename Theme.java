import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class Theme {
    public static final Color NAVY = new Color(20, 45, 30);         // Dark Forest
    public static final Color NAVY_LIGHT = new Color(35, 65, 45);   // Lighter Forest
    public static final Color PANEL = new Color(245, 242, 235);     // Soft Beige (Light)
    public static final Color PANEL_ALT = new Color(235, 230, 220); // Darker Beige
    public static final Color BLUE = new Color(60, 130, 80);        // Leaf Green
    public static final Color TEAL = new Color(40, 160, 120);       // Mint
    public static final Color BG = new Color(210, 215, 210);        // Pale stone background
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color TEXT = new Color(30, 40, 30);         // Dark green-black
    public static final Color MUTED = new Color(100, 110, 100);
    public static final Color BORDER = new Color(200, 195, 185);
    public static final Color RED = new Color(200, 60, 50);         // Terracotta
    public static final Color ORANGE = new Color(210, 140, 50);     // Amber
    public static final Color GREEN = new Color(40, 150, 80);       // Bright Leaf

    public static void applyGlobalUIManager() {
        UIManager.put("Button.foreground", WHITE);
        UIManager.put("Button.background", NAVY);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("Panel.background", BG);
    }

    public static JPanel pagePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(24, 26, 24, 26));
        return p;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), new EmptyBorder(1, 1, 1, 1)));
        return p;
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setForeground(NAVY); // Dark green titles
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(MUTED);
        return l;
    }

    public static JButton createButton(String text, Color color) {
        JButton b = new JButton(text);
        styleButton(b, color, WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(new EmptyBorder(10, 15, 10, 15));
        return b;
    }

    public static void styleButton(JButton b, Color bg, Color fg) {
        b.setOpaque(true); b.setContentAreaFilled(true); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setBackground(bg); b.setForeground(fg);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    public static void styleTextField(JTextField f) {
        f.setBackground(PANEL_ALT); f.setForeground(TEXT); f.setCaretColor(NAVY);
        f.setSelectionColor(new Color(180, 220, 190)); f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 9, 8, 9)));
    }

    public static void styleCombo(JComboBox<String> c) {
        c.setBackground(PANEL_ALT); c.setForeground(TEXT);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    public static void styleTable(JTable t) {
        t.setFont(new Font("SansSerif", Font.PLAIN, 12)); 
        t.setForeground(TEXT);
        t.setBackground(PANEL); 
        t.setRowHeight(36); 
        t.setShowVerticalLines(false);
        t.setGridColor(BORDER); 
        t.setSelectionBackground(new Color(180, 210, 190));
        t.setSelectionForeground(TEXT); 
        t.setFillsViewportHeight(true); 
        t.setAutoCreateRowSorter(true);
        
        javax.swing.table.JTableHeader header = t.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setForeground(TEXT); 
        header.setBackground(PANEL_ALT);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setBackground(PANEL_ALT);
                label.setForeground(TEXT); 
                label.setFont(new Font("SansSerif", Font.BOLD, 11));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                label.setOpaque(true); 
                return label;
            }
        });
    }

    public static void showError(Component parent, String msg, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent, msg + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
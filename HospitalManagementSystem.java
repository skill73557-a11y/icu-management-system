import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HospitalManagementSystem extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JLabel pageTitle = new JLabel("Dashboard");
    
    // Changed to 8 buttons
    private final JButton[] navButtons = new JButton[8];
    private final String[] pageNames = {
        "Dashboard", "Patients", "ICU Beds", "Emergency Queue", "Doctors", "Pharmacy", "Reports", "Manage Data"
    };

    // Keep references to panels so we can refresh them
    private DashboardPanel dashboardPanel;
    private PatientsPanel patientsPanel;
    private ICUBedsPanel icuBedsPanel;
    private EmergencyPanel emergencyPanel;
    private DoctorsPanel doctorsPanel;
    private PharmacyPanel pharmacyPanel;
    private ReportsPanel reportsPanel;
    private ManageDataPanel manageDataPanel;

    private Timer autoRefreshTimer;

    public HospitalManagementSystem() {
        super("CarePoint | Ultimate Hospital Command");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 750));
        setSize(1400, 880);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        buildSidebar();
        buildMainArea();
        startAutoRefresh(); // Start the 3-second sync
        
        showPage(0);
    }

    private void startAutoRefresh() {
        // Runs every 3000 milliseconds (3 seconds)
        autoRefreshTimer = new Timer(3000, e -> {
            // Reload data on all panels silently in the background
            if (dashboardPanel != null) dashboardPanel.loadData();
            if (patientsPanel != null) patientsPanel.loadData();
            if (icuBedsPanel != null) icuBedsPanel.loadData();
            if (emergencyPanel != null) emergencyPanel.loadData();
            if (doctorsPanel != null) doctorsPanel.loadData();
            if (pharmacyPanel != null) pharmacyPanel.loadData();
            if (reportsPanel != null) reportsPanel.loadData();
            if (manageDataPanel != null) manageDataPanel.loadData();
        });
        autoRefreshTimer.start();
    }

    private void buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(250, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(27, 20, 28, 15));

        JLabel logo = new JLabel("+", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Theme.WHITE);
        logo.setBackground(Theme.BLUE);
        logo.setOpaque(true);
        logo.setPreferredSize(new Dimension(50, 50));
        logo.setMaximumSize(new Dimension(50, 50));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandName = new JLabel("CarePoint");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 24));
        brandName.setForeground(Theme.WHITE);
        brandName.setBorder(new EmptyBorder(15, 0, 3, 0));
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logo); brand.add(brandName);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(5, 12, 0, 12));

        for (int i = 0; i < pageNames.length; i++) {
            final int index = i;
            JButton b = new JButton("   " + pageNames[i]);
            Theme.styleButton(b, Theme.NAVY, Theme.WHITE);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(13, 12, 13, 8));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.addActionListener(e -> showPage(index));
            navButtons[i] = b;
            nav.add(b);
            nav.add(Box.createVerticalStrut(5));
        }
        sidebar.add(nav, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);
    }

    private void buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);

        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Theme.PANEL);
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(17, 28, 17, 25)));
        
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 23));
        pageTitle.setForeground(Theme.TEXT);
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);
        
        JButton refresh = Theme.createButton("Refresh Data", Theme.BLUE);
        refresh.addActionListener(e -> {
            dashboardPanel.loadData();
            patientsPanel.loadData();
            icuBedsPanel.loadData();
            emergencyPanel.loadData();
            doctorsPanel.loadData();
            pharmacyPanel.loadData();
            reportsPanel.loadData();
            manageDataPanel.loadData();
        });
        
        right.add(refresh);
        topbar.add(pageTitle, BorderLayout.WEST);
        topbar.add(right, BorderLayout.EAST);

        contentPanel.setBackground(Theme.BG);
        
        // Initialize panels
        dashboardPanel = new DashboardPanel();
        patientsPanel = new PatientsPanel();
        icuBedsPanel = new ICUBedsPanel();
        emergencyPanel = new EmergencyPanel();
        doctorsPanel = new DoctorsPanel();
        pharmacyPanel = new PharmacyPanel();
        reportsPanel = new ReportsPanel();
        manageDataPanel = new ManageDataPanel();

        // Add to layout
        contentPanel.add(dashboardPanel, "0");
        contentPanel.add(patientsPanel, "1");
        contentPanel.add(icuBedsPanel, "2");
        contentPanel.add(emergencyPanel, "3");
        contentPanel.add(doctorsPanel, "4");
        contentPanel.add(pharmacyPanel, "5");
        contentPanel.add(reportsPanel, "6");
        contentPanel.add(manageDataPanel, "7");

        main.add(topbar, BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private void showPage(int index) {
        pageTitle.setText(pageNames[index]);
        cardLayout.show(contentPanel, String.valueOf(index));
        for (int i = 0; i < navButtons.length; i++) {
            boolean selected = i == index;
            Theme.styleButton(navButtons[i], selected ? Theme.BLUE : Theme.NAVY, Theme.WHITE);
            navButtons[i].setFont(new Font("SansSerif", selected ? Font.BOLD : Font.PLAIN, 14));
        }
    }
}
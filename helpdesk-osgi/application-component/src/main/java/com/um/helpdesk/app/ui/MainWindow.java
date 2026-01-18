package com.um.helpdesk.app.ui;

import com.um.helpdesk.app.coordinator.ApplicationCoordinator;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private ApplicationCoordinator coordinator;
    private JTextArea logArea;

    public MainWindow(ApplicationCoordinator coordinator) {
        this.coordinator = coordinator;
        initUI();
    }

    private void initUI() {
        setTitle("UM Helpdesk System (OSGi Edition)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Let OSGi handle closing
        setLayout(new BorderLayout());

        // 1. Control Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10));

        JButton btnUser = new JButton("👤 Create Test User");
        btnUser.addActionListener(e -> log(coordinator.registerUser("Ali", "Student")));

        JButton btnReport = new JButton("📊 Generate Report");
        btnReport.addActionListener(e -> log(coordinator.generateReport("Monthly_Stats", "PDF")));

        JButton btnNotify = new JButton("🔔 Send Notification");
        btnNotify.addActionListener(e -> log(coordinator.sendNotification("System Maintenance at 12PM")));

        panel.add(btnUser);
        panel.add(btnReport);
        panel.add(btnNotify);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 2. Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Output"));

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }
}
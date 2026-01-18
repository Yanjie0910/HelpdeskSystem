package com.um.helpdesk.app;

import com.um.helpdesk.app.coordinator.ApplicationCoordinator;
import com.um.helpdesk.app.ui.MainWindow;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.swing.*;

public class Activator implements BundleActivator {

    private MainWindow window;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting Application UI...");

        // 1. Initialize Coordinator with OSGi Context
        ApplicationCoordinator coordinator = new ApplicationCoordinator(context);

        // 2. Launch UI on the Swing Event Thread (Standard Practice)
        SwingUtilities.invokeLater(() -> {
            window = new MainWindow(coordinator);
            window.setVisible(true);
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping Application UI...");
        // Close window when OSGi bundle stops
        if (window != null) {
            SwingUtilities.invokeLater(() -> window.dispose());
        }
    }
}
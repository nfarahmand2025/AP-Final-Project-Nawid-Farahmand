// Package declaration: groups related classes under the 'com.mall' namespace.
package com.mall;

// Imports for the main UI frame and Swing utilities.
import com.mall.ui.MainFrame;
import javax.swing.*;



//change made
// Main application class. Contains the program entry point (main method).
public class App {
    public static void main(String[] args) {
        try {
            // Attempt to set the UI look and feel to match the host system's theme.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If setting the look and feel fails, log the error to standard error output.
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        // Schedule creation and display of the GUI on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            // Instantiate the application's main window (custom JFrame subclass).
            MainFrame frame = new MainFrame();
            // Make the main window visible to the user.
            frame.setVisible(true);
        });
    }
}

package ikube.app;

import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-06-2015
 */
public class SystemMonitor {

    public static void main(final String[] args) {
        new SystemMonitor();
        THREAD.sleep(60000);
        System.exit(0);
    }

    TrayIcon trayIcon;
    private ActionListener showListener;
    private ActionListener exitListener;
    private MouseListener mouseListener;
    private ActionListener actionListener;
    private ActionListener settingsListener;

    private String tsURI;

    public SystemMonitor() {
        if (SystemTray.isSupported()) {
            createActions();

            SystemTray tray = SystemTray.getSystemTray();
            File startDirectory = FILE.findFileRecursively(new File("."), "ikube", "favicon.ico");
            String imagePath = FILE.cleanFilePath(startDirectory.getAbsolutePath());
            Image image = Toolkit.getDefaultToolkit().getImage(imagePath);

            PopupMenu popup = new PopupMenu();
            MenuItem settingsItem = new MenuItem("Settings");
            settingsItem.addActionListener(settingsListener);
            popup.add(settingsItem);

            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem quitItem = new MenuItem("Quit");
            quitItem.addActionListener(exitListener);
            popup.add(quitItem);

            trayIcon = new TrayIcon(image, null, popup);

            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(mouseListener);
            trayIcon.addActionListener(actionListener);

            try {
                tray.add(trayIcon);
            } catch (final Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Creates actions for different actions
     */
    private void createActions() {
        //Open URL
        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //MainWindow.showGUI();
                try {
                    new URL(tsURI);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };

        //empty, to do?
        mouseListener = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }
        };

        // open settings
        settingsListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // SettingsWindow.showSettings();
            }
        };

        // show URL
        showListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new URL(tsURI);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };

        //quit program
        exitListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Application.quit();
            }
        };
    }

    //method to set ts URL for URL actions
    public void setTsURI(String string) {
        System.out.println("Setting uri " + string);
        tsURI = string;
    }

}

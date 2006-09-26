/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import jorgan.App;

/**
 * Panel that displays information about jOrgan.
 */
public class AboutPanel extends JPanel {

    private static ResourceBundle resources = ResourceBundle
            .getBundle("jorgan.gui.resources");

    /**
     * The icon used to display the about image.
     */
    private ImageIcon icon = new ImageIcon(getClass().getResource(
            "img/about.gif"));

    /**
     * The label used to display the version of jOrgan.
     */
    private JLabel label = new JLabel();

    /**
     * Creata an about panel.
     */
    public AboutPanel() {
        setLayout(new BorderLayout());

        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon
                .getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.drawImage(icon.getImage(), 0, 0, this);

        String version = App.getVersion();
        int width = g.getFontMetrics().stringWidth(version);
        g.setFont(new Font("Sans Serif", Font.PLAIN, 14));
        g.setColor(Color.black);
        g.drawString(version, icon.getIconWidth() - width - 10, 182);

        icon.setImage(image);

        label.setIcon(icon);
        add(label);
    }

    /**
     * Utility method to show an about panel in a dialog.
     * 
     * @param parent
     *            the frame to use for the dialogs parent
     * @return the dialog showing the about panel
     */
    public static void showInDialog(JFrame parent) {

        AboutPanel aboutPanel = new AboutPanel();

        JDialog dialog = new JDialog(parent, resources
                .getString("action.about.description"), true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(aboutPanel);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JWindow splash;

    /**
     * Utility method to show an about panel in a window.
     */
    public static void showSplash() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AboutPanel aboutPanel = new AboutPanel();
                aboutPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

                splash = new JWindow();
                splash.setContentPane(aboutPanel);
                splash.pack();
                splash.setLocationRelativeTo(null);
                splash.setVisible(true);
            }
        });
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }        
    }

    public static void hideSplash() {
        if (splash != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    splash.dispose();
                    splash = null;
                }
            });
        }
    }
}
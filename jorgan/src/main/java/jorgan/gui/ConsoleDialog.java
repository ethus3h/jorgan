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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;

import jorgan.disposition.Console;
import jorgan.docs.Documents;
import jorgan.swing.CardPanel;

/**
 * JDialog subclass to show a console <em>full screen</em>.
 */
public class ConsoleDialog extends JDialog {

    private static ResourceBundle resources = ResourceBundle
            .getBundle("jorgan.gui.resources");

    /**
     * The handler of mouse events.
     */
    private MouseHandler mouseHandler = new MouseHandler();

    private JScrollPane scrollPane = new JScrollPane(
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private CardPanel cardPanel = new CardPanel();

    private JPopupMenu popup = new JPopupMenu();

    private ButtonGroup group = new ButtonGroup();

    private OrganSession session;

    /**
     * Create a dialog.
     */
    public ConsoleDialog(JFrame owner, GraphicsConfiguration configuration) {
        super(owner, null, false, configuration);

        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Rectangle bounds = getGraphicsConfiguration().getBounds();
        setBounds(bounds);

        scrollPane.setBorder(null);
        getContentPane().add(scrollPane);
        scrollPane.setViewportView(cardPanel);

        // popup.addSeparator();
        // popup.add(new MemoryAction());
        popup.addSeparator();
        popup.add(new CloseAction());
    }

    public void setOrgan(OrganSession session) {
        this.session = session;
    }

    /**
     * Add a console to be shown in <em>full screen</em>.
     */
    public void addConsole(final Console console) {

        ConsolePanel consolePanel = new ConsolePanel();
        consolePanel.setOrgan(session);
        consolePanel.setConsole(console);

        consolePanel.addMouseListener(mouseHandler);
        consolePanel.addMouseMotionListener(mouseHandler);

        cardPanel.addCard(consolePanel, console);

        final JCheckBoxMenuItem check = new JCheckBoxMenuItem(Documents.getInstance().getDisplayName(console));
        check.getModel().setGroup(group);
        check.setSelected(true);
        check.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (check.isSelected()) {
                    cardPanel.selectCard(console);
                }
            }
        });
        popup.add(check, 0);
    }
    /**
     * The handler for mouse events.
     */
    private class MouseHandler extends MouseInputAdapter implements
            ActionListener {

        private Timer timer;

        private int deltaX;

        private int deltaY;

        public MouseHandler() {
            timer = new Timer(50, this);
        }

        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        protected void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void mouseExited(MouseEvent e) {
            if (timer.isRunning()) {
                timer.stop();
            }
        }

        public void mouseMoved(MouseEvent e) {

            if (popup.isVisible()) {
                return;
            }

            Rectangle rect = scrollPane.getViewport().getViewRect();

            int x = e.getX() - (rect.x + rect.width / 2);
            int y = e.getY() - (rect.y + rect.height / 2);

            deltaX = (int) (Math.pow((double) x / (rect.width / 2), 5) * (rect.width / 5));
            deltaY = (int) (Math.pow((double) y / (rect.height / 2), 5) * (rect.height / 5));

            if (deltaX != 0 || deltaY != 0) {
                if (!timer.isRunning()) {
                    timer.start();
                }
            } else {
                if (timer.isRunning()) {
                    timer.stop();
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            // must change horizontal and vertical value separately
            // or otherwise scrollpane will not use blitting :(
            scrollPane.getHorizontalScrollBar().setValue(
                    scrollPane.getHorizontalScrollBar().getValue() + deltaX);
            scrollPane.getVerticalScrollBar().setValue(
                    scrollPane.getVerticalScrollBar().getValue() + deltaY);
        }
    }

    private class CloseAction extends AbstractAction {

        public CloseAction() {
            putValue(Action.NAME, resources
                    .getString("fullScreen.action.close.name"));

            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), this);
            getRootPane().getActionMap().put(this, this);
        }

        public void actionPerformed(ActionEvent ev) {
            ConsoleDialog.this.setVisible(false);
        }
    }

    public static ConsoleDialog create(JFrame owner, String screen) {

        if (screen == null) {
            throw new IllegalArgumentException("screen must not be null");
        }

        GraphicsConfiguration configuration = null;
        GraphicsEnvironment environment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = environment.getScreenDevices();
        for (int d = 0; d < devices.length; d++) {
            if (devices[d].getIDstring().equals(screen)) {
                configuration = devices[d].getDefaultConfiguration();
            }
        }

        ConsoleDialog dialog = new ConsoleDialog(owner, configuration);

        return dialog;
    }

    public static String getDefaultSceen() {
        GraphicsEnvironment environment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice device = environment.getDefaultScreenDevice();

        return device.getIDstring();
    }
}
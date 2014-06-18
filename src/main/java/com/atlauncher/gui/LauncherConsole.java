/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.evnt.ConsoleCloseEvent;
import com.atlauncher.evnt.ConsoleOpenEvent;
import com.atlauncher.evnt.RelocalizationEvent;
import com.atlauncher.evnt.ReskinEvent;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.listener.ReskinListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.ReskinManager;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Utils;

public class LauncherConsole extends JFrame implements RelocalizationListener, ReskinListener {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = -3538990021922025818L;
    private JScrollPane scrollPane;
    public Console console;
    private ConsoleBottomBar bottomBar;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    public LauncherConsole() {
        setTitle("ATLauncher Console " + Constants.VERSION);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setMinimumSize(new Dimension(600, 400));
        this.setLayout(new BorderLayout());

        console = new Console();
        console.setFont(App.THEME.getConsoleFont().deriveFont(Utils.getBaseFontSize()));
        console.setForeground(App.THEME.getConsoleTextColor());
        console.setSelectionColor(App.THEME.getSelectionColor());

        setupContextMenu(); // Setup the right click menu

        bottomBar = new ConsoleBottomBar();

        scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
        RelocalizationManager.addListener(this);
        ReskinManager.addListener(this);
    }

    @Override
    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if (flag) {
            ConsoleOpenManager.post(new ConsoleOpenEvent());
        } else {
            ConsoleCloseManager.post(new ConsoleCloseEvent());
        }
    }

    private void setupContextMenu() {
        contextMenu = new JPopupMenu();

        copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringSelection text = new StringSelection(console.getSelectedText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            }
        });
        contextMenu.add(copy);

        console.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (console.getSelectedText() != null) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        contextMenu.show(console, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    /**
     * Returns a string with the text currently in the console
     * 
     * @return String Console Text
     */
    public String getLog() {
        return console.getText();
    }

    public void showKillMinecraft() {
        bottomBar.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        bottomBar.hideKillMinecraft();
    }

    public void setupLanguage() {
        copy.setText(App.settings.getLocalizedString("common.copy"));
        bottomBar.setupLanguage();
    }

    public void clearConsole() {
        console.setText(null);
    }

    @Override
    public void onRelocalization(RelocalizationEvent event) {
        copy.setText(App.settings.getLocalizedString("common.copy"));
        bottomBar.setupLanguage();
    }

    @Override
    public void onReskin(ReskinEvent event) {
        console.setFont(App.THEME.getConsoleFont().deriveFont(Utils.getBaseFontSize()));
        console.setForeground(App.THEME.getConsoleTextColor());
        console.setSelectionColor(App.THEME.getSelectionColor());
        console.setBackground(App.THEME.getBaseColor());
        console.revalidate();
        console.repaint();
    }
}
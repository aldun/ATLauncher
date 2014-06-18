/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Account;
import com.atlauncher.data.Language;
import com.atlauncher.data.Status;
import com.atlauncher.evnt.ConsoleCloseEvent;
import com.atlauncher.evnt.ConsoleOpenEvent;
import com.atlauncher.evnt.RelocalizationEvent;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.BottomBar;
import com.atlauncher.gui.dialogs.GithubIssueReporterDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class LauncherBottomBar extends BottomBar implements RelocalizationListener{
    private JPanel leftSide;
    private JPanel middle;

    private Account fillerAccount;
    private boolean dontSave = false;

    private JButton toggleConsole;
    private JButton openFolder;
    private JButton updateData;
    private final JButton submitError = new JButton("Submit Bug");
    private JComboBox<Account> username;

    private JLabel statusIcon;

    public LauncherBottomBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50)); // Make the bottom bar at least
        // 50 pixels high

        submitError.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new GithubIssueReporterDialog(null).setVisible(true);
                    }
                });
            }
        });

        leftSide = new JPanel();
        leftSide.setLayout(new GridBagLayout());
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        createButtons();
        setupListeners();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        leftSide.add(toggleConsole, gbc);
        gbc.gridx++;
        leftSide.add(openFolder, gbc);
        gbc.gridx++;
        leftSide.add(updateData, gbc);
        // gbc.gridx++;
        // leftSide.add(submitError, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(username, gbc);
        gbc.gridx++;
        middle.add(statusIcon, gbc);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        add(rightSide, BorderLayout.EAST);
        RelocalizationManager.addListener(this);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        toggleConsole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.getConsole().setVisible(!App.settings.isConsoleVisible());
            }
        });
        openFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openExplorer(App.settings.getBaseDir());
            }
        });
        updateData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final ProgressDialog dialog = new ProgressDialog(App.settings
                        .getLocalizedString("common.checkingforupdates"), 0, App.settings
                        .getLocalizedString("common.checkingforupdates"), "Aborting Update Check!");
                dialog.addThread(new Thread() {
                    public void run() {
                        if (App.settings.hasUpdatedFiles()) {
                            App.settings.reloadLauncherData();
                        }
                        dialog.close();
                    }

                    ;
                });
                dialog.start();
            }
        });
        username.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!dontSave) {
                        App.settings.switchAccount((Account) username.getSelectedItem());
                    }
                }
            }
        });
        ConsoleCloseManager.addListener(new ConsoleCloseListener() {
            @Override
            public void onConsoleClose(ConsoleCloseEvent event) {
                toggleConsole.setText(Language.INSTANCE.localize("console.show"));
            }
        });
        ConsoleOpenManager.addListener(new ConsoleOpenListener(){
            @Override
            public void onConsoleOpen(ConsoleOpenEvent event) {
                toggleConsole.setText(Language.INSTANCE.localize("console.hide"));
            }
        });
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        if (App.settings.isConsoleVisible()) {
            toggleConsole = new JButton(Language.INSTANCE.localize("console.hide"));
        } else {
            toggleConsole = new JButton(Language.INSTANCE.localize("console.show"));
        }

        openFolder = new JButton(Language.INSTANCE.localize("common.openfolder"));
        updateData = new JButton(Language.INSTANCE.localize("common.updatedata"));

        username = new JComboBox<Account>();
        username.setRenderer(new AccountsDropDownRenderer());
        fillerAccount = new Account(Language.INSTANCE.localize("account.select"));
        username.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }
        Account active = App.settings.getAccount();
        if (active == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(active);
        }

        statusIcon = new JLabel(Utils.getIconImage("/assets/image/StatusWhite.png")) {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        statusIcon.setBorder(BorderFactory.createEmptyBorder());
        statusIcon.setToolTipText(Language.INSTANCE.localize("status.minecraft.checking"));
    }

    /**
     * Update the status icon to show the current Minecraft server status.
     * 
     * @param status
     *            The status of servers
     */
    public void updateStatus(Status status) {
        switch (status) {
            case UNKNOWN:
                statusIcon.setToolTipText(App.settings
                        .getLocalizedString("status.minecraft.checking"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusWhite.png"));
                break;
            case ONLINE:
                statusIcon.setToolTipText(App.settings
                        .getLocalizedString("status.minecraft.online"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusGreen.png"));
                break;
            case OFFLINE:
                statusIcon.setToolTipText(App.settings
                        .getLocalizedString("status.minecraft.offline"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusRed.png"));
                break;
            case PARTIAL:
                statusIcon.setToolTipText(App.settings
                        .getLocalizedString("status.minecraft.partial"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusYellow.png"));
                break;
            default:
                break;
        }
    }

    public void reloadAccounts() {
        dontSave = true;
        username.removeAllItems();
        username.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }
        if (App.settings.getAccount() == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(App.settings.getAccount());
        }
        dontSave = false;
    }

    @Override
    public void onRelocalization(RelocalizationEvent event) {
        if (App.settings.getConsole().isVisible()) {
            toggleConsole.setText(Language.INSTANCE.localize("console.hide"));
        } else {
            toggleConsole.setText(Language.INSTANCE.localize("console.show"));
        }
        this.updateData.setText(Language.INSTANCE.localize("common.updatedata"));
        this.openFolder.setText(Language.INSTANCE.localize("common.openfolder"));
    }
}

/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.utils.MCQuery;
import com.atlauncher.utils.Utils;

import de.zh32.pingtest.QueryVersion;

public class AddEditServerForCheckerDialog extends JDialog implements ActionListener {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 3385411077046354453L;
    private JPanel middle;
    private JPanel bottom;

    private JLabel serverNameLabel;
    private JTextField serverName;

    private JLabel serverHostLabel;
    private JTextField serverHost;

    private JLabel serverPortLabel;
    private JTextField serverPort;

    private JButton addEditButton;
    private JButton closeButton;

    private MinecraftServer serverEditing = null;

    public AddEditServerForCheckerDialog(MinecraftServer minecraftServer) {
        super(null, App.settings.getLocalizedString((minecraftServer == null ? "tools.addserver"
                : "tools.editserver")), ModalityType.APPLICATION_MODAL);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        setupComponents();

        if (minecraftServer != null) {
            this.serverEditing = minecraftServer;
            addEditButton.setText(App.settings.getLocalizedString("common.edit"));
            serverName.setText(minecraftServer.getName());
            serverHost.setText(minecraftServer.getHost());
            serverPort.setText(minecraftServer.getPort() + "");
        }

        setVisible(true);
    }

    private void setupComponents() {
        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Server Name

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        serverNameLabel = new JLabel(App.settings.getLocalizedString("tools.serverchecker.name")
                + ": ");
        middle.add(serverNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverName = new JTextField(16);
        middle.add(serverName, gbc);

        // Server Host/IP

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        serverHostLabel = new JLabel(App.settings.getLocalizedString("tools.serverchecker.ip")
                + ": ");
        middle.add(serverHostLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverHost = new JTextField(16);
        middle.add(serverHost, gbc);

        // Server Port

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        serverPortLabel = new JLabel(App.settings.getLocalizedString("tools.serverchecker.port")
                + ": ");
        middle.add(serverPortLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverPort = new JTextField(16);
        serverPort.setText("25565");
        middle.add(serverPort, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        addEditButton = new JButton(App.settings.getLocalizedString("common.add"));
        addEditButton.addActionListener(this);
        bottom.add(addEditButton);

        closeButton = new JButton(App.settings.getLocalizedString("common.close"));
        closeButton.addActionListener(this);
        bottom.add(closeButton);

        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public boolean isValidPort() {
        if (serverPort.getText().isEmpty()
                || Integer.parseInt(serverPort.getText().replaceAll("[^0-9]", "")) < 1
                || Integer.parseInt(serverPort.getText().replaceAll("[^0-9]", "")) > 65535) {
            return false;
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addEditButton) {
            if (serverName.getText().isEmpty() || serverHost.getText().isEmpty()
                    || serverPort.getText().isEmpty()) {
                JOptionPane.showMessageDialog(App.settings.getParent(),
                        App.settings.getLocalizedString("tools.serverchecker.notallfields"),
                        App.settings.getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
            } else if (!isValidPort()) {
                JOptionPane.showMessageDialog(App.settings.getParent(),
                        App.settings.getLocalizedString("settings.proxyportinvalid"),
                        App.settings.getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
            } else {
                String name = serverName.getText();
                final String host = serverHost.getText();
                final int port = Integer.parseInt(serverPort.getText().replaceAll("[^0-9]", ""));
                QueryVersion qv = null;

                final ProgressDialog dialog = new ProgressDialog(
                        App.settings.getLocalizedString("tools.serverchecker.checkingserver"), 0,
                        App.settings.getLocalizedString("tools.serverchecker.checkingserver"),
                        "Cancelled Server Check!");
                dialog.addThread(new Thread() {
                    @Override
                    public void run() {
                        dialog.setReturnValue(MCQuery.getMinecraftServerQueryVersion(host, port));
                        dialog.close();
                    }
                });
                dialog.start();

                if (dialog.getReturnValue() != null) {
                    qv = (QueryVersion) dialog.getReturnValue();
                }

                if (qv == null) {
                    JOptionPane.showMessageDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("tools.serverchecker.couldntconnect"),
                            App.settings.getLocalizedString("common.error"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    String message = App.settings
                            .getLocalizedString((this.serverEditing == null ? "tools.serverchecker.serveradded"
                                    : "tools.serverchecker.serveredited"));
                    JOptionPane.showMessageDialog(App.settings.getParent(), message, message,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (this.serverEditing == null) {
                        App.settings.addCheckingServer(new MinecraftServer(name, host, port, qv));
                    } else {
                        this.serverEditing.setName(name);
                        this.serverEditing.setHost(host);
                        this.serverEditing.setPort(port);
                        this.serverEditing.setQueryVersion(qv);
                        App.settings.saveCheckingServers();
                    }
                    close();
                }
            }
        } else if (e.getSource() == closeButton) {
            close();
        }
    }

    public void close() {
        setVisible(false);
        dispose();
    }

}
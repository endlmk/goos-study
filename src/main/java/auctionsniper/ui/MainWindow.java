package auctionsniper.ui;

import auctionsniper.UserRequestListener;
import auctionsniper.util.Announcer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String APPLICATION_TITLE = "Auction Sniper";
    public static final String JOIN_BUTTON_NAME = "join button";
    public static final String NEW_ITEM_ID_NAME = "item id";
    private static final String SNIPER_TABLE_NAME = "Snipers Table";
    private final Announcer<UserRequestListener> userRequests = Announcer.to(UserRequestListener.class);

    public MainWindow(SnipersTableModel snipers) {
        super(APPLICATION_TITLE);
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable(snipers), makeControls());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JPanel makeControls() {
        JPanel controls = new JPanel(new FlowLayout());
        final JTextField itemIDField = new JTextField();
        itemIDField.setColumns(25);
        itemIDField.setName(NEW_ITEM_ID_NAME);
        controls.add(itemIDField);

        JButton joinAuctionButton = new JButton("Join Auction");
        joinAuctionButton.setName(JOIN_BUTTON_NAME);
        joinAuctionButton.addActionListener(e -> {
            try {
                userRequests.announce().joinAuction(itemIDField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        controls.add(joinAuctionButton);

        return controls;
    }

    private void fillContentPane(JTable snipersTable, JPanel controls) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controls, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable(TableModel snipers) {
        final JTable snipersTable = new JTable(snipers);
        snipersTable.setName(SNIPER_TABLE_NAME);
        return snipersTable;
    }

    public void addUserRequestListener(UserRequestListener userRequestListener) {
        userRequests.addListener(userRequestListener);
    }
}

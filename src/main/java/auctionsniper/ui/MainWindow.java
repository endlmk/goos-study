package auctionsniper.ui;

import auctionsniper.SniperPortfolio;
import auctionsniper.UserRequestListener;
import auctionsniper.util.Announcer;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class MainWindow extends JFrame {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String APPLICATION_TITLE = "Auction Sniper";
    public static final String JOIN_BUTTON_NAME = "join button";
    public static final String NEW_ITEM_ID_NAME = "item id";
    public static final String NEW_ITEM_STOP_PRICE_NAME = "stop price";
    private static final String SNIPER_TABLE_NAME = "Snipers Table";
    private final Announcer<UserRequestListener> userRequests = Announcer.to(UserRequestListener.class);

    public MainWindow(SniperPortfolio portfolio) {
        super(APPLICATION_TITLE);
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable(portfolio), makeControls());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JPanel makeControls() {
        final JTextField itemIDField = itemIDField();
        final JFormattedTextField stopPriceField = stopPriceField();

        JPanel controls = new JPanel(new FlowLayout());
        controls.add(new JLabel("Item:"));
        controls.add(itemIDField);
        controls.add(new JLabel("Stop price:"));
        controls.add(stopPriceField);

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

    private JTextField itemIDField() {
        JTextField itemIDField = new JTextField();
        itemIDField.setColumns(10);
        itemIDField.setName(NEW_ITEM_ID_NAME);
        return itemIDField;
    }

    private JFormattedTextField stopPriceField() {
        JFormattedTextField stopPriceField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        stopPriceField.setColumns(7);
        stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME);
        return stopPriceField;
    }
    private void fillContentPane(JTable snipersTable, JPanel controls) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controls, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable(SniperPortfolio portfolio) {
        SnipersTableModel model = new SnipersTableModel();
        portfolio.addPortfolioListener(model);
        final JTable snipersTable = new JTable(model);
        snipersTable.setName(SNIPER_TABLE_NAME);
        return snipersTable;
    }

    public void addUserRequestListener(UserRequestListener userRequestListener) {
        userRequests.addListener(userRequestListener);
    }
}

package auctionsniper.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    private final SnipersTableModel snipers;
    private static final String SNIPER_TABLE_NAME = "Snipers Table";

    public MainWindow(SnipersTableModel snipers) {
        super("Auction Sniper");
        this.snipers = snipers;
        setName(MAIN_WINDOW_NAME);
        fillContentPane(makeSnipersTable());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fillContentPane(JTable snipersTable) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable() {
        final JTable snipersTable = new JTable(snipers);
        snipersTable.setName(SNIPER_TABLE_NAME);
        return snipersTable;
    }
}

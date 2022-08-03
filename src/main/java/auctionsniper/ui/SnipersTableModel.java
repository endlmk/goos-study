package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;

import javax.swing.table.AbstractTableModel;

public class SnipersTableModel extends AbstractTableModel {
    private final static SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0, SniperState.JOINING);
    private SniperSnapshot sniperSnapshot = STARTING_UP;
    private static final String[] STATUS_TEXT = {
            MainWindow.STATUS_JOINING,
            MainWindow.STATUS_BIDDING,
            MainWindow.STATUS_WINNING,
            MainWindow.STATUS_LOST,
            MainWindow.STATUS_WON
    };
    private String state = MainWindow.STATUS_JOINING;

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (Column.at(columnIndex))
        {
            case ITEM_IDENTIFIER -> { return sniperSnapshot.itemId(); }
            case LAST_PRICE -> { return sniperSnapshot.lastPrice(); }
            case LAST_BID -> { return sniperSnapshot.lastBid(); }
            case SNIPER_STATUS -> { return state; }
            default -> throw new IllegalArgumentException("No column at " + columnIndex);
        }
    }

    public void setStatusText(String newStatusText) {
        state = newStatusText;
        fireTableRowsUpdated(0, 0);
    }

    public void sniperStatusChanged(SniperSnapshot newSniperSnapshot) {
        sniperSnapshot = newSniperSnapshot;
        state = STATUS_TEXT[newSniperSnapshot.state().ordinal()];
        fireTableRowsUpdated(0, 0);
    }
}

package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private final SnipersTableModel snipers = new SnipersTableModel();
    private MainWindow ui;

    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    public static final String AUCTION_RESOURCE = "Auction";
    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";
    private List<Auction> notToBeGCd = new ArrayList<>();

    public Main () throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow(snipers));
    }

    public static void main (String... args) throws Exception {
        Main main = new Main();
        XMPPAuctionHouse auctionHouse = XMPPAuctionHouse.connect(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
        main.disconnectWhenUICloses(auctionHouse);
        main.addUserRequestListenerFor(auctionHouse);
    }

    private void addUserRequestListenerFor(final AuctionHouse auctionHouse) {
        ui.addUserRequestListener(itemId -> {
            snipers.addSniper(SniperSnapshot.joining(itemId));
            Auction auction = auctionHouse.auctionFor(itemId);
            notToBeGCd.add(auction);

            auction.addAuctionEventListener(new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers)));
            auction.join();
        });
    }

    private void disconnectWhenUICloses(final XMPPAuctionHouse auctionHouse) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auctionHouse.disconnect();
            }
        });
    }

    public static class SwingThreadSniperListener implements SniperListener {
        private final SniperListener delegate;
        SwingThreadSniperListener(SniperListener delegate) {
            this.delegate = delegate;
        }
        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            SwingUtilities.invokeLater(() -> delegate.sniperStateChanged(sniperSnapshot));
        }
    }

}
package auctionsniper;

import auctionsniper.ui.SnipersTableModel;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.swing.*;
import java.util.ArrayList;

public class SniperLauncher implements UserRequestListener {
    private final SnipersTableModel snipers;
    private final AuctionHouse auctionHouse;
    private ArrayList<Auction> notToBeGCd = new ArrayList<>();

    public SniperLauncher(AuctionHouse auctionHouse, SnipersTableModel snipers)
    {
        this.auctionHouse = auctionHouse;
        this.snipers = snipers;
    }

    @Override
    public void joinAuction(String itemId) throws XmppStringprepException {
        snipers.addSniper(SniperSnapshot.joining(itemId));
        Auction auction = auctionHouse.auctionFor(itemId);
        notToBeGCd.add(auction);

        AuctionSniper sniper = new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers));
        auction.addAuctionEventListener(sniper);
        auction.join();
    }

    private static class SwingThreadSniperListener implements SniperListener {
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

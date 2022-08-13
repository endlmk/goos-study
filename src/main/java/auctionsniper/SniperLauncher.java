package auctionsniper;

import org.jxmpp.stringprep.XmppStringprepException;

public class SniperLauncher implements UserRequestListener {
    private final SniperCollector collector;
    private final AuctionHouse auctionHouse;

    public SniperLauncher(AuctionHouse auctionHouse, SniperCollector snipers)
    {
        this.auctionHouse = auctionHouse;
        this.collector = snipers;
    }

    @Override
    public void joinAuction(String itemId) throws XmppStringprepException {
        Auction auction = auctionHouse.auctionFor(itemId);
        AuctionSniper sniper = new AuctionSniper(itemId, auction);
        auction.addAuctionEventListener(sniper);
        collector.addSniper(sniper);
        auction.join();
    }
}

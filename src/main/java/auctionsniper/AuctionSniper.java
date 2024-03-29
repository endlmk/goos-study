package auctionsniper;

import auctionsniper.util.Announcer;

public class AuctionSniper implements AuctionEventListener {
    private final Announcer<SniperListener> listeners = Announcer.to(SniperListener.class);
    private final Auction auction;
    private SniperSnapshot snapshot;
    private final Item item;

    public AuctionSniper(Item item, Auction auction) {
        this.item = item;
        this.auction = auction;
        this.snapshot = SniperSnapshot.joining(item.identifier());
    }

    public void addSniperListener(SniperListener sniperListener) {
        listeners.addListener(sniperListener);
    }

    public void auctionClosed() {
        snapshot = snapshot.closed();
        notifyChange();
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        switch(priceSource)
        {
            case FromSniper -> snapshot = snapshot.winning(price);
            case FromOtherBidder -> {
                int bid = price + increment;
                if(item.allowsBid(bid)) {
                    auction.bid(bid);
                    snapshot = snapshot.bidding(price, bid);
                }
                else {
                    snapshot = snapshot.losing(price);
                }
            }
        }
        notifyChange();
    }

    @Override
    public void auctionFailed() {
        snapshot = snapshot.failed();
        notifyChange();
    }

    private void notifyChange() {
        listeners.announce().sniperStateChanged(snapshot);
    }

    public SniperSnapshot getSnapshot() {
        return snapshot;
    }
}

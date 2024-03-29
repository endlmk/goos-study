package auctionsniper;

import com.objogate.exception.Defect;

public enum SniperState {
    JOINING {
        public SniperState whenAuctionClosed() { return LOST; }
    },
    BIDDING{
        public SniperState whenAuctionClosed() { return LOST; }
    },
    WINNING{
        public SniperState whenAuctionClosed() { return WON; }
    },
    LOSING{
        public SniperState whenAuctionClosed() { return LOST; }
    },
    LOST,
    WON,
    FAILED;

    public SniperState whenAuctionClosed() {
        throw new Defect("Auction is already closed");
    }
}

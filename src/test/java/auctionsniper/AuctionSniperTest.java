package auctionsniper;

import endtoend.AuctionSniperDriver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AuctionSniperTest {
    private final Auction auction = Mockito.mock(Auction.class);
    private final SniperListener sniperListener = Mockito.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);

    @Test
    public void reportsLostWhenAuctionClosed() {
        sniper.auctionClosed();
        Mockito.verify(sniperListener).sniperLost();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrived() {
        final int price = 1001;
        final int increment = 25;

        sniper.currentPrice(price, increment);
        Mockito.verify(auction).bid(price + increment);
        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperBidding();
    }
}

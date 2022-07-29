package auctionsniper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static auctionsniper.AuctionEventListener.*;

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

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
        Mockito.verify(auction).bid(price + increment);
        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperBidding();
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        Mockito.verify(sniperListener).sniperWinning();
    }
}

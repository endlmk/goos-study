package auctionsniper;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.hamcrest.MatcherAssert.assertThat;

public class AuctionSniperTest {
    private final Auction auction = Mockito.mock(Auction.class);
    private final SniperListener sniperListener = Mockito.spy(new SniperListenerStub());
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);
    private enum SniperStateForSpy { Lost, Bidding, Winning }
    private SniperStateForSpy state = SniperStateForSpy.Lost;
    @Test
    public void reportsLostWhenAuctionClosedImmediately() {
        sniper.auctionClosed();
        Mockito.verify(sniperListener).sniperLost();
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(123,45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();
        Mockito.verify(sniperListener).sniperLost();
        assertThat(state, IsEqual.equalTo(SniperStateForSpy.Bidding));
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

    private class SniperListenerStub implements SniperListener {
        @Override
        public void sniperLost() {

        }

        @Override
        public void sniperBidding() {
            state = SniperStateForSpy.Bidding;
        }

        @Override
        public void sniperWinning() {

        }
    }
}

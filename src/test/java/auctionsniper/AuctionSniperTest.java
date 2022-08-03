package auctionsniper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AuctionSniperTest {
    private static final String ITEM_ID = "item-54321";
    private final Auction auction = Mockito.mock(Auction.class);
    private final SniperListener sniperListener = Mockito.spy(new SniperListenerStub());
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener, ITEM_ID);
    private SniperState state = SniperState.JOINING;
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
        assertThat(state, equalTo(SniperState.BIDDING));
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrived() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
        Mockito.verify(auction).bid(bid);
        Mockito.verify(sniperListener, Mockito.atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);
        Mockito.verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING)
        );
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        sniper.auctionClosed();

        assertThat(state, equalTo(SniperState.WINNING));
        Mockito.verify(sniperListener).sniperWon();
    }

    private class SniperListenerStub implements SniperListener {
        @Override
        public void sniperLost() {

        }

        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            state = sniperSnapshot.state();
        }

        @Override
        public void sniperWon() {

        }
    }
}

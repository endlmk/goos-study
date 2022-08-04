package auctionsniper;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class AuctionSniperTest {
    private static final String ITEM_ID = "item-54321";
    private final Auction auction = mock(Auction.class);
    private final SniperListener sniperListener = spy(new SniperListenerStub());
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);
    private SniperState state = SniperState.JOINING;
    @Test
    public void reportsLostWhenAuctionClosedImmediately() {
        sniper.auctionClosed();
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.LOST)
        );
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        sniper.currentPrice(123,45, PriceSource.FromOtherBidder);
        sniper.auctionClosed();
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 123, 168, SniperState.LOST)
        );
        assertThat(state, equalTo(SniperState.LOST));
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrived() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);
        verify(auction).bid(bid);
        verify(sniperListener, atLeastOnce()).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING));
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 135, 135, SniperState.WINNING)
        );
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        ArgumentCaptor<SniperSnapshot> snapshotCaptor = ArgumentCaptor.forClass(SniperSnapshot.class);

        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        sniper.auctionClosed();
        verify(sniperListener, times(2)).sniperStateChanged(snapshotCaptor.capture());
        List<SniperSnapshot> result = snapshotCaptor.getAllValues();
        assertThat(result.get(0).state(), equalTo(SniperState.WINNING));
        assertThat(result.get(1).state(), equalTo(SniperState.WON));
        assertThat(state, equalTo(SniperState.WON));
    }

    private class SniperListenerStub implements SniperListener {
        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            state = sniperSnapshot.state();
        }
    }
}

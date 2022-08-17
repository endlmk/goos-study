package auctionsniper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class AuctionSniperTest {
    private static final String ITEM_ID = "item-54321";
    private static final Item ITEM = new Item(ITEM_ID, 1234);
    private final Auction auction = mock(Auction.class);
    private final SniperListener sniperListener = spy(new SniperListenerStub());
    private final AuctionSniper sniper = new AuctionSniper(ITEM, auction);
    private SniperState state = SniperState.JOINING;

    @BeforeEach
    public void addSniperListener() {
        sniper.addSniperListener(sniperListener);
    }
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
    @Test
    public void doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        sniper.currentPrice(1233, 25, PriceSource.FromOtherBidder);

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1233, 0, SniperState.LOSING)
        );
    }
    @Test
    public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder);

        int bid = 123 + 45;
        verify(auction).bid(bid);
        verify(sniperListener).sniperStateChanged(
            new SniperSnapshot(ITEM_ID, 2345, bid, SniperState.LOSING)
        );
    }

    @Test
    public void doseNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(168, 45, PriceSource.FromSniper);
        sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder);

        int bid = 123 + 45;
        verify(auction).bid(bid);
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 168, 168, SniperState.WINNING)
        );
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 2345, bid, SniperState.LOSING)
        );
    }
    @Test
    public void continuesToBeLosingOnceStopPriceHasBeenReached() {
        sniper.currentPrice(1233, 25, PriceSource.FromOtherBidder);
        sniper.currentPrice(1258, 25, PriceSource.FromOtherBidder);

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1233, 0, SniperState.LOSING)
        );
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1258, 0, SniperState.LOSING)
        );
    }

    @Test
    public void reportsLostIfAuctionClosesWhenLosing() {
        sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1230, 0, SniperState.LOSING)
        );
        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 1230, 0, SniperState.LOST)
        );
    }

    @Test
    public void reportsFailedIfAuctionFailsWhenBidding() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.auctionFailed();

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED)
        );
    }

    @Test
    public void reportsFailedIfAuctionFailsImmediately() {
        sniper.auctionFailed();

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED)
        );
    }

    @Test
    public void reportsFailedIfAuctionFailsWhenWinning() {
        sniper.currentPrice(1230, 45, PriceSource.FromOtherBidder);
        sniper.auctionFailed();

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED)
        );
    }

    @Test
    public void reportsFailedIfAuctionFailsWhenLosing() {
        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);
        sniper.auctionFailed();

        verify(sniperListener).sniperStateChanged(
                new SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED)
        );
    }

    private class SniperListenerStub implements SniperListener {
        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            state = sniperSnapshot.state();
        }
    }
}

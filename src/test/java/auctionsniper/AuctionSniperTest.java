package auctionsniper;

import endtoend.AuctionSniperDriver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AuctionSniperTest {
    private final SniperListener sniperListener = Mockito.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(sniperListener);

    @Test
    public void reportsLostWhenAuctionClosed() {
        sniper.auctionClosed();
        Mockito.verify(sniperListener).sniperLost();
    }
}

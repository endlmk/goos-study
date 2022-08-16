package auctionsniper;

import org.junit.jupiter.api.Test;
import org.jxmpp.stringprep.XmppStringprepException;

import static org.mockito.Mockito.*;

public class SniperLauncherTest {
    private final AuctionHouse auctionHouse = mock(AuctionHouse.class);
    private final Auction auction = mock(Auction.class);
    private final SniperCollector sniperCollector =  mock(SniperCollector.class);

    private final SniperLauncher sniperLauncher = new SniperLauncher(auctionHouse, sniperCollector);

    @Test
    public void addsNewSniperToCollectionAndThenJoinsAuction() throws XmppStringprepException {
        final Item item = new Item("item 123", 456);
        when(auctionHouse.auctionFor(item.identifier())).thenReturn(auction);

        sniperLauncher.joinAuction(item);

        verify(auction).addAuctionEventListener(any(AuctionSniper.class));
        verify(sniperCollector).addSniper(any(AuctionSniper.class));
        verify(auction).join();
    }
}

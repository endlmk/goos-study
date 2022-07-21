package endtoend;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.UnknownHostException;

public class AuctionSniperEndToEndTest {
    private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
    private final ApplicationRunner application = new ApplicationRunner();

    public AuctionSniperEndToEndTest() throws XmppStringprepException {
    }

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFromSniper();
        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @Test
    public void sniperMakeAHigherBitButLoses() throws Exception {
        auction.startSellingItem();

        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFromSniper();

        auction.reportPrice(1000, 98, "other bidder");

        application.hasShownSniperIsBidding();
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.announceClosed();
        application.showsSniperHasLostAuction();
    }

    @AfterEach
    public void stopAuction() {
        auction.stop();
    }

    @AfterEach
    public void stopApplication() {
        application.stop();
    }
}

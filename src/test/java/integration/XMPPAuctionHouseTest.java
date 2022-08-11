package integration;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.XMPPAuctionHouse;
import endtoend.ApplicationRunner;
import endtoend.FakeAuctionServer;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class XMPPAuctionHouseTest {
    private final FakeAuctionServer auctionServer = new FakeAuctionServer("item-54321");
    private XMPPAuctionHouse auctionHouse;

    public XMPPAuctionHouseTest() throws XmppStringprepException {
    }

    @BeforeEach
    public void openConnection() throws IOException, SmackException, XMPPException, InterruptedException {
        auctionHouse = XMPPAuctionHouse.connect(FakeAuctionServer.XMPP_HOSTNAME, ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD);
    }

    @AfterEach
    public void closeConnection() {
        auctionHouse.disconnect();
    }

    @BeforeEach
    public void startAuction() throws SmackException, IOException, XMPPException, InterruptedException {
        auctionServer.startSellingItem();
    }

    @AfterEach
    public void stopAuction() {
        auctionServer.stop();
    }

    @Test
    public void receivesEventsFromAuctionServerAfterJoining() throws XmppStringprepException, InterruptedException, SmackException.NotConnectedException {
        CountDownLatch auctionWasClosed = new CountDownLatch(1);

        Auction auction = auctionHouse.auctionFor(auctionServer.getItemId());
        auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed));

        auction.join();
        auctionServer.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        auctionServer.announceClosed();

        assertTrue(auctionWasClosed.await(2, TimeUnit.SECONDS), "should have been closed");
    }

    private AuctionEventListener auctionClosedListener(CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            @Override
            public void auctionClosed() {
                auctionWasClosed.countDown();
            }

            @Override
            public void currentPrice(int price, int increment, PriceSource fromOtherBidder) {

            }
        };
    }
}

package auctionsniper.XMPP;

import auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.junit.jupiter.api.Test;
import org.jxmpp.jid.EntityBareJid;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.mockito.Mockito.*;

public class AuctionMessageTranslatorTest {
    public static final Chat UNUSED_CHAT = null;
    public static final EntityBareJid FAKE_AUCTION_ADDRESS = mock(EntityBareJid.class);
    public static final String SNIPER_ID = "sniper";
    private final AuctionEventListener listener = mock(AuctionEventListener.class);
    private final XMPPFailureReporter failureReporter = mock(XMPPFailureReporter.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener, FAKE_AUCTION_ADDRESS, failureReporter);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        Message message = StanzaBuilder
                .buildMessage()
                .setBody("SOLVersion: 1.1; Event: CLOSE;")
                .build();

        translator.newIncomingMessage(FAKE_AUCTION_ADDRESS, message, UNUSED_CHAT);

        verify(listener).auctionClosed();
    }

    @Test
    public void notifiesBidDetailWhenCurrentPriceMessageReceivedFromOtherBidder() {
        Message message = StanzaBuilder
                .buildMessage()
                .setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
                .build();

        translator.newIncomingMessage(FAKE_AUCTION_ADDRESS, message ,UNUSED_CHAT);

        verify(listener, times(1)).currentPrice(192, 7, PriceSource.FromOtherBidder);
    }

    @Test
    public void notifiesBidDetailWhenCurrentPriceMessageReceivedFromSniper() {
        Message message = StanzaBuilder
                .buildMessage()
                .setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: " + SNIPER_ID + ";")
                .build();

        translator.newIncomingMessage(FAKE_AUCTION_ADDRESS, message ,UNUSED_CHAT);

        verify(listener, times(1)).currentPrice(192, 7, PriceSource.FromSniper);
    }

    @Test
    public void notifiesAuctionFailedWhenBadMessageReceived() {
        String badMessage = "a bad message";
        Message message = StanzaBuilder
                .buildMessage()
                .setBody(badMessage)
                .build();

        translator.newIncomingMessage(FAKE_AUCTION_ADDRESS, message, UNUSED_CHAT);

        verify(listener, times(1)).auctionFailed();
        verify(failureReporter).cannotTranslateMessage(eq(SNIPER_ID), eq(badMessage), any(Exception.class));
    }

    @Test
    public void notifiesAuctionFailedWhenEventTypeMissing() {
        String badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";";
        Message message = StanzaBuilder
                .buildMessage()
                .setBody(badMessage)
                .build();

        translator.newIncomingMessage(FAKE_AUCTION_ADDRESS, message, UNUSED_CHAT);

        verify(listener, times(1)).auctionFailed();
        verify(failureReporter).cannotTranslateMessage(eq(SNIPER_ID), eq(badMessage), any(Exception.class));
    }
}

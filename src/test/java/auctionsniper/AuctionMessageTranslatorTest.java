package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.junit.jupiter.api.Test;
import org.jxmpp.jid.EntityBareJid;
import org.mockito.Mockito;

public class AuctionMessageTranslatorTest {
    public static final Chat UNUSED_CHAT = null;
    public static final EntityBareJid UNUSED_ADDRESS = null;
    private final AuctionEventListener listener = Mockito.mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(listener);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        Message message = StanzaBuilder
                .buildMessage()
                .setBody("SOLVersion: 1.1; Event: CLOSE;")
                .build();

        translator.newIncomingMessage(UNUSED_ADDRESS, message, UNUSED_CHAT);

        Mockito.verify(listener).auctionClosed();
    }

    @Test
    public void notifiesBidDetailWhenCurrentPriceMessageReceived() {
        Message message = StanzaBuilder
                .buildMessage()
                .setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
                .build();

        translator.newIncomingMessage(UNUSED_ADDRESS, message ,UNUSED_CHAT);

        Mockito.verify(listener, Mockito.times(1)).currentPrice(192, 7);
    }
}

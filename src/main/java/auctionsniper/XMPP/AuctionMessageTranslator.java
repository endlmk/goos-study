package auctionsniper.XMPP;

import auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashMap;

import static auctionsniper.AuctionEventListener.*;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private final String sniperId;
    private final AuctionEventListener listener;
    private final EntityBareJid auctionItemId;
    private final XMPPFailureReporter failureReporter;

    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener, EntityBareJid auctionItemId, XMPPFailureReporter failureReporter) {
        this.sniperId = sniperId;
        this.listener = listener;
        this.auctionItemId = auctionItemId;
        this.failureReporter = failureReporter;
    }
    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        if(from.compareTo(auctionItemId) != 0)
        {
            return;
        }


        String body = message.getBody();

        try {
            translate(body);
        } catch (Exception parseException) {
            failureReporter.cannotTranslateMessage(sniperId, body, parseException);
            listener.auctionFailed();
        }
    }

    private void translate(String body) throws AuctionEvent.MissingValueException {
        AuctionEvent event = AuctionEvent.from(body);
        String eventType = event.type();
        if("CLOSE".equals(eventType)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(eventType)) {
            listener.currentPrice(event.currentPrice(),
                    event.increment(),
                    event.isFrom(sniperId));
        }
    }

    private static class AuctionEvent {
        HashMap<String, String> fields = new HashMap<>();

        public String type() throws MissingValueException {
            return get("Event");
        }

        public int currentPrice() throws MissingValueException {
            return getInt("CurrentPrice");
        }

        public int increment() throws MissingValueException {
            return getInt("Increment");
        }

        private String get(String fieldName) throws MissingValueException {
            String value = fields.get(fieldName);
            if(value == null)
            {
                throw new MissingValueException(fieldName);
            }
            return value;
        }

        private int getInt(String fieldName) throws MissingValueException {
            return Integer.parseInt(get(fieldName));
        }

        private void addField(String element) {
            String[] pair = element.split(":");
            fields.put(pair[0].trim(), pair[1].trim());
        }
        static AuctionEvent from(String messageBody) {
            AuctionEvent event = new AuctionEvent();
            for (String element : fieldsIn(messageBody)) {
                event.addField(element);
            }
            return event;
        }

        private static String[] fieldsIn(String messageBody) {
            return messageBody.split(";");
        }

        public PriceSource isFrom(String sniperId) throws MissingValueException {
            return sniperId.equals(bidder()) ? PriceSource.FromSniper : PriceSource.FromOtherBidder;
        }

        private String bidder() throws MissingValueException {
            return get("Bidder");
        }

        private static class MissingValueException extends Exception {
            public MissingValueException(String fieldName) {
                super("Missing value for " + fieldName);
            }
        }
    }
}

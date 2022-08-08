package auctionsniper;

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
    public AuctionMessageTranslator(String sniperId, AuctionEventListener listener, EntityBareJid auctionItemId) {
        this.sniperId = sniperId;
        this.listener = listener;
        this.auctionItemId = auctionItemId;
    }
    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        if(from.compareTo(auctionItemId) != 0)
        {
            return;
        }
        AuctionEvent event = AuctionEvent.from(message.getBody());
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

        public String type() {
            return get("Event");
        }

        public int currentPrice() {
            return getInt("CurrentPrice");
        }

        public int increment() {
            return getInt("Increment");
        }

        private String get(String fieldName) {
            return fields.get(fieldName);
        }

        private int getInt(String fieldName) {
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

        public PriceSource isFrom(String sniperId) {
            return sniperId.equals(bidder()) ? PriceSource.FromSniper : PriceSource.FromOtherBidder;
        }

        private String bidder() {
            return get("Bidder");
        }
    }
}

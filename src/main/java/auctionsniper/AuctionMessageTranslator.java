package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashMap;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private final AuctionEventListener listener;
    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }
    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        HashMap<String, String> event = unpackEventFrom(message);

        String type = event.get("Event");
        if("CLOSE".equals(type)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(type)) {
            listener.currentPrice(Integer.parseInt(event.get("CurrentPrice")),
                    Integer.parseInt(event.get("Increment")));
        }
    }

    private HashMap<String, String> unpackEventFrom(Message message) {
        HashMap<String, String> event = new HashMap<>();
        for (String element : message.getBody().split(";")) {
            String[] pair = element.split(":");
            event.put(pair[0].trim(), pair[1].trim());
        }
        return event;
    }
}

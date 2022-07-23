package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private final AuctionEventListener listener;
    public AuctionMessageTranslator(AuctionEventListener l) {
        listener = l;
    }
    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        listener.auctionClosed();
    }
}

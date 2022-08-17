package auctionsniper.XMPP;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.Main;
import auctionsniper.util.Announcer;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;

public class XMPPAuction implements Auction {
    private final Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
    private final Chat chat;
    private final ChatManager manager;

    public XMPPAuction(AbstractXMPPConnection connection, EntityBareJid auctionId) {
        AuctionMessageTranslator translator = translateFor(connection, auctionId);
        manager = ChatManager.getInstanceFor(connection);
        chat = manager.chatWith(auctionId);
        manager.addIncomingListener(translator);
        addAuctionEventListener(chatDisconnectFor(translator));
    }

    private AuctionMessageTranslator translateFor(AbstractXMPPConnection connection, EntityBareJid auctionId) {
        return new AuctionMessageTranslator(
                connection.getUser().asEntityBareJidString(),
                auctionEventListeners.announce(),
                auctionId);
    }

    private AuctionEventListener chatDisconnectFor(AuctionMessageTranslator translator) {
        return new AuctionEventListener() {
            @Override
            public void auctionClosed() {

            }

            @Override
            public void currentPrice(int price, int increment, PriceSource fromOtherBidder) {

            }

            @Override
            public void auctionFailed() {
                manager.removeIncomingListener(translator);
            }
        };
    }

    @Override
    public void bid(int amount) {
        sendMessage(String.format(Main.BID_COMMAND_FORMAT, amount));
    }

    @Override
    public void join() {
        sendMessage(Main.JOIN_COMMAND_FORMAT);
    }

    @Override
    public void addAuctionEventListener(AuctionEventListener listener) {
        auctionEventListeners.addListener(listener);
    }

    private void sendMessage(final String message) {
        try {
            chat.send(message);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

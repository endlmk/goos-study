package endtoend;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FakeAuctionServer {
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String XMPP_HOSTNAME = "localhost";
    public static final String AUCTION_PASSWORD = "auction";

    private final SingleMessageListener messageListener = new SingleMessageListener();
    private final String itemId;
    private final AbstractXMPPConnection connection;

    public FakeAuctionServer(String itemId) throws XmppStringprepException {
        this.itemId = itemId;
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(XMPP_HOSTNAME)
                .setUsernameAndPassword(format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
        this.connection = new XMPPTCPConnection(config);
    }

    public void startSellingItem() throws XMPPException, SmackException, IOException, InterruptedException {
        connection.connect();
        connection.login();
        ChatManager manager = ChatManager.getInstanceFor(connection);
        manager.addIncomingListener(messageListener);
    }
    public String getItemId() {
        return itemId;
    }

    public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
        messageListener.receiveAMessage();
    }

    public void announceClosed() throws SmackException.NotConnectedException, InterruptedException {
        if(messageListener.GetCurrentChat() != null) {
            messageListener.GetCurrentChat().send("");
        }
    }

    public void stop() {
        connection.disconnect();
    }
}

class SingleMessageListener implements IncomingChatMessageListener
{
    private Chat currentChat;
    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);
    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        messages.add(message);
        currentChat = chat;
    }

    public void receiveAMessage() throws InterruptedException {
        assertThat(messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
    }

    public Chat GetCurrentChat() {
        return currentChat;
    }
}
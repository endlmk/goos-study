package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Main {
    private final SnipersTableModel snipers = new SnipersTableModel();
    private MainWindow ui;

    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final String AUCTION_RESOURCE = "Auction";
    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    private static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    public Main () throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow(snipers));
    }

    public static void main (String... args) throws Exception {
        Main main = new Main();
        AbstractXMPPConnection connection = connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]);
        main.disconnectWhenUICloses(connection);
        main.addUserRequestListenerFor(connection);
    }

    private static AbstractXMPPConnection connection(String hostname, String username, String password) throws IOException, SmackException, XMPPException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(hostname)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login(username, password);
        return connection;
    }

    private static EntityBareJid auctionId(String itemId, XMPPConnection connection) throws XmppStringprepException {
        String address = String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain().toString());
        return JidCreate.entityBareFrom(address);
    }

    private void addUserRequestListenerFor(AbstractXMPPConnection connection) {
        ui.addUserRequestListener(itemId -> {
            snipers.addSniper(SniperSnapshot.joining(itemId));
            ChatManager manager = ChatManager.getInstanceFor(connection);
            final Chat chat = manager.chatWith(auctionId(itemId, connection));

            Auction auction = new XMPPAuction(chat);
            manager.addIncomingListener(new AuctionMessageTranslator(connection.getUser().asEntityBareJidString(),
                    new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers)),
                    auctionId(itemId, connection)));
            auction.join();
        });
    }

    private void disconnectWhenUICloses(final AbstractXMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    public static class SwingThreadSniperListener implements SniperListener {
        private final SniperListener delegate;
        SwingThreadSniperListener(SniperListener delegate) {
            this.delegate = delegate;
        }
        @Override
        public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
            SwingUtilities.invokeLater(() -> delegate.sniperStateChanged(sniperSnapshot));
        }
    }

    public static class XMPPAuction implements Auction {
        private final Chat chat;

        public XMPPAuction(Chat chat) {
             this.chat = chat;
        }

        @Override
        public void bid(int amount) {
            sendMessage(String.format(BID_COMMAND_FORMAT, amount));
        }

        @Override
        public void join() {
            sendMessage(JOIN_COMMAND_FORMAT);
        }

        private void sendMessage(final String message) {
            try {
                chat.send(message);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
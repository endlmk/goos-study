package auctionsniper;

import auctionsniper.ui.MainWindow;
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
    private MainWindow ui;

    private static final int ARG_HOSTNAME = 0;
    private static final int ARG_USERNAME = 1;
    private static final int ARG_PASSWORD = 2;
    private static final int ARG_ITEM_ID = 3;
    private static final String AUCTION_RESOURCE = "Auction";
    private static final String ITEM_ID_AS_LOGIN = "auction-%s";
    private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    private static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    public Main () throws Exception {
        startUserInterface();
    }

    public static void main (String... args) throws Exception {
        Main main = new Main();
        main.joinAuction(connection(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD]),
                args[ARG_ITEM_ID]);
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow());
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

    private void joinAuction(AbstractXMPPConnection connection, String itemId) throws XmppStringprepException {
        disconnectWhenUICloses(connection);
        ChatManager manager = ChatManager.getInstanceFor(connection);
        final Chat chat = manager.chatWith(auctionId(itemId, connection));

        Auction auction = new XMPPAuction(chat);
        manager.addIncomingListener(new AuctionMessageTranslator(connection.getUser().asEntityBareJidString(), new AuctionSniper(auction, new SniperStateDisplayer())));
        auction.join();
    }

    private void disconnectWhenUICloses(final AbstractXMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    public class SniperStateDisplayer implements SniperListener {
        @Override
        public void sniperBidding() {
            showStatus(MainWindow.STATUS_BIDDING);
        }

        @Override
        public void sniperLost() {
            showStatus(MainWindow.STATUS_LOST);
        }

        @Override
        public void sniperWinning() {
            showStatus(MainWindow.STATUS_WINNING);
        }

        @Override
        public void sniperWon() {
            showStatus(MainWindow.STATUS_WON);
        }

        private void showStatus(String statusWinning) {
            SwingUtilities.invokeLater(() -> ui.showStatusText(statusWinning));
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
package auctionsniper;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

public class XMPPAuctionHouse implements AuctionHouse {
    private final AbstractXMPPConnection connection;

    public XMPPAuctionHouse(AbstractXMPPConnection connection) {
        this.connection = connection;
    }

    public static XMPPAuctionHouse connect(String hostname, String username, String password) throws IOException, SmackException, XMPPException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(hostname)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login(username, password, Resourcepart.from(Main.AUCTION_RESOURCE));
        return new XMPPAuctionHouse(connection);
    }

    @Override
    public Auction auctionFor(String itemId) throws XmppStringprepException {
        return new XMPPAuction(connection, auctionId(itemId, connection));
    }

    public void disconnect() {
        connection.disconnect();
    }

    private static EntityBareJid auctionId(String itemId, XMPPConnection connection) throws XmppStringprepException {
        String address = String.format(Main.AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain().toString());
        return JidCreate.entityBareFrom(address);
    }
}

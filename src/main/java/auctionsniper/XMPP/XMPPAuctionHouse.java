package auctionsniper.XMPP;

import auctionsniper.Auction;
import auctionsniper.AuctionHouse;
import auctionsniper.Main;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.apache.commons.io.FilenameUtils.getFullPath;

public class XMPPAuctionHouse implements AuctionHouse {
    private static final String LOGGER_NAME = "auction-sniper";
    public static final String LOG_FILE_NAME = "auction-sniper.log";
    private final AbstractXMPPConnection connection;
    private final XMPPFailureReporter failureReporter;

    public XMPPAuctionHouse(AbstractXMPPConnection connection) throws XMPPAuctionException {
        
        this.connection = connection;
        this.failureReporter = new LoggingXMPPFailureReporter(makeLogger());
    }

    private Logger makeLogger() throws XMPPAuctionException {
        Logger logger = Logger.getLogger(LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.addHandler(simpleFileHandler());
        return logger;
    }

    private FileHandler simpleFileHandler() throws XMPPAuctionException {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_NAME);
            handler.setFormatter(new SimpleFormatter());
            return handler;

        } catch (Exception e) {
          throw new XMPPAuctionException("Could not create logger FileHandler " + getFullPath(LOG_FILE_NAME), e);
        }
    }

    public static XMPPAuctionHouse connect(String hostname, String username, String password) throws XMPPAuctionException, XmppStringprepException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(hostname)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);

        try {
            connection.connect();
            connection.login(username, password, Resourcepart.from(Main.AUCTION_RESOURCE));
            return new XMPPAuctionHouse(connection);
        } catch (Exception e) {
            throw new XMPPAuctionException("Could not connect to auction: " + connection, e);
        }
    }

    @Override
    public Auction auctionFor(String itemId) throws XmppStringprepException {
        return new XMPPAuction(connection, auctionId(itemId, connection), failureReporter);
    }

    public void disconnect() {
        connection.disconnect();
    }

    private static EntityBareJid auctionId(String itemId, XMPPConnection connection) throws XmppStringprepException {
        String address = String.format(Main.AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain().toString());
        return JidCreate.entityBareFrom(address);
    }
}

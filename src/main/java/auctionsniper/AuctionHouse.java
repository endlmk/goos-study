package auctionsniper;

import org.jxmpp.stringprep.XmppStringprepException;

public interface AuctionHouse {
    Auction auctionFor(String itemId) throws XmppStringprepException;
}

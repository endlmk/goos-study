package auctionsniper;

import org.jxmpp.stringprep.XmppStringprepException;

import java.util.EventListener;

public interface UserRequestListener extends EventListener {
    void joinAuction(String itemId) throws XmppStringprepException;
}

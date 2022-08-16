package auctionsniper;

import org.jxmpp.stringprep.XmppStringprepException;

import java.util.EventListener;

public interface UserRequestListener extends EventListener {
    void joinAuction(Item item) throws XmppStringprepException;
}

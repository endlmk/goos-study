package endtoend;


import auctionsniper.Main;
import auctionsniper.SniperState;
import auctionsniper.ui.MainWindow;

import static auctionsniper.ui.SnipersTableModel.textFor;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    private static final String XMPP_HOST_NAME = "localhost";
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOST_NAME;
    private AuctionSniperDriver driver;

    public void startBiddingIn(final FakeAuctionServer... auctions) {
        Thread thread = new Thread("Test Application") {
            @Override public void run() {
                try {
                    Main.main(arguments(auctions));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

        driver = new AuctionSniperDriver(1000);
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitles();
    }

    private static String[] arguments(FakeAuctionServer... auctions) {
        String[] arguments = new String[auctions.length + 3];
        arguments[0] = XMPP_HOST_NAME;
        arguments[1] = SNIPER_ID;
        arguments[2] = SNIPER_PASSWORD;
        for (int i = 0; i < auctions.length; i++) {
            arguments[i + 3] = auctions[i].getItemId();
        }
        return arguments;
    }

    public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId() , lastPrice, lastBid, textFor(SniperState.BIDDING));
    }
    public void showsSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBids) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBids, textFor(SniperState.LOST));
    }

    public void stop() {
        if(driver != null) {
            driver.dispose();
        }
    }

    public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        driver.showsSniperStatus(auction.getItemId(), winningBid, winningBid, textFor(SniperState.WINNING)); }

    public void showsSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastPrice, textFor(SniperState.WON));
    }
}

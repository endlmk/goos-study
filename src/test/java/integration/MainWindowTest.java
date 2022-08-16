package integration;

import auctionsniper.Item;
import auctionsniper.SniperPortfolio;
import auctionsniper.ui.MainWindow;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import endtoend.AuctionSniperDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class MainWindowTest {
    private final MainWindow mainWindow = new MainWindow(new SniperPortfolio());
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);

    @BeforeAll
    public static void setupKeyboardLayout() {
        System.setProperty("com.objogate.wl.keyboard", "US");
    }

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        final ValueMatcherProbe<Item> itemProbe =
                new ValueMatcherProbe<>(equalTo(new Item("an item-id", 789)), "join request");
        mainWindow.addUserRequestListener(
                itemProbe::setReceivedValue);

        driver.startBiddingFor("an item-id", 789);
        driver.check(itemProbe);
    }
}

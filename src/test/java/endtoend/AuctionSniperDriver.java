package endtoend;

import auctionsniper.ui.MainWindow;
import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JTableDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

import static com.objogate.wl.swing.matcher.JLabelTextMatcher.withLabelText;
import static org.hamcrest.Matchers.equalTo;

public class AuctionSniperDriver extends JFrameDriver {

    public AuctionSniperDriver(int timeoutMillis) {
        //noinspection unchecked
        super(new GesturePerformer(),
                    JFrameDriver.topLevelFrame(
                        named(MainWindow.MAIN_WINDOW_NAME),
                        showingOnScreen()),
                new AWTEventQueueProber(timeoutMillis,100));
    }

    public void showsSniperStatus(String statusText) {
        //noinspection unchecked
        new JTableDriver(this).hasCell(withLabelText(equalTo(statusText)));
    }
}

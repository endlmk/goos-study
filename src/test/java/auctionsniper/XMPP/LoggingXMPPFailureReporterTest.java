package auctionsniper.XMPP;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LoggingXMPPFailureReporterTest {
    final Logger logger = mock(Logger.class);
    final LoggingXMPPFailureReporter reporter = new LoggingXMPPFailureReporter(logger);

    @AfterAll
    public static void resetLogging() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void writesMessageTranslationFailureToLog() {
        reporter.cannotTranslateMessage("auction id", "bad message", new Exception("bad"));

        verify(logger).severe("<auction id> "
            + "Could not translate message \"bad message\" "
            + "because \"java.lang.Exception: bad\"");
    }
}

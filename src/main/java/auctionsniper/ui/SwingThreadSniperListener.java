package auctionsniper.ui;

import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;

import javax.swing.*;

public class SwingThreadSniperListener implements SniperListener {
    private final SniperListener delegate;
    SwingThreadSniperListener(SniperListener delegate) {
        this.delegate = delegate;
    }
    @Override
    public void sniperStateChanged(SniperSnapshot sniperSnapshot) {
        SwingUtilities.invokeLater(() -> delegate.sniperStateChanged(sniperSnapshot));
    }
}
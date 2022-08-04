package auctionsniper.ui;

import auctionsniper.SniperSnapshot;

import static auctionsniper.ui.SnipersTableModel.textFor;

public enum Column {
    ITEM_IDENTIFIER {
        public Object valueIn(SniperSnapshot snapshot) {
            return snapshot.itemId();
        }
    },
    LAST_PRICE {
        public Object valueIn(SniperSnapshot snapshot) {
            return snapshot.lastPrice();
        }
    },
    LAST_BID {
        public Object valueIn(SniperSnapshot snapshot) {
            return snapshot.lastBid();
        }
    },
    SNIPER_STATUS{
        public Object valueIn(SniperSnapshot snapshot) {
            return textFor(snapshot.state());
        }
    };

    abstract public Object valueIn(SniperSnapshot snapshot);

    public static Column at(int offset) { return values()[offset]; }
}

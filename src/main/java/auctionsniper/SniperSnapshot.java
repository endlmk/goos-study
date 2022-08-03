package auctionsniper;

// 値型としてrecordを使用する. recordが使えるのはJava16以降.
public record SniperSnapshot(String itemId, int lastPrice, int lastBid, SniperState state) {
    public static SniperSnapshot joining(String itemId) {
        return new SniperSnapshot(itemId, 0, 0, SniperState.JOINING);
    }

    public SniperSnapshot winning(int newLastPrice) {
        return new SniperSnapshot(itemId(), newLastPrice, lastBid(), SniperState.WINNING);
    }

    public SniperSnapshot bidding(int newLastPrice, int newLastBid) {
        return new SniperSnapshot(itemId(), newLastPrice, newLastBid, SniperState.BIDDING);
    }
}

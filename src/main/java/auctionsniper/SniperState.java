package auctionsniper;

// 値型としてrecordを使用する. recordが使えるのはJava16以降.
public record SniperState(String itemId, int lastPrice, int lastBid) {
}

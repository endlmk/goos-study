package auctionsniper;

public record Item(String identifier, int stopPrice) {
    public boolean allowsBid(int bid) {
        return bid <= stopPrice;
    }
}

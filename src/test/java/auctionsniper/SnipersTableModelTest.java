package auctionsniper;

import auctionsniper.ui.Column;
import auctionsniper.ui.SnipersTableModel;
import com.objogate.exception.Defect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static auctionsniper.ui.SnipersTableModel.textFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SnipersTableModelTest {
    private static final String ITEM_ID = "item 0";
    @Mock
    private TableModelListener listener;
    private final SnipersTableModel model = new SnipersTableModel();

    private final AuctionSniper sniper = new AuctionSniper(new Item(ITEM_ID, 234), null);

    @BeforeEach
    public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test
    public void hasEnoughColumns() {
        assertThat(model.getColumnCount(), equalTo(Column.values().length));
    }

    @Test
    public void setsSniperValuesInColumns() {
        ArgumentCaptor<TableModelEvent> tableEventCaptor = ArgumentCaptor.forClass(TableModelEvent.class);

        SniperSnapshot bidding = sniper.getSnapshot().bidding(555, 666);

        model.sniperAdded(sniper);
        model.sniperStateChanged(bidding);

        assertRowMatchesSnapshot(0, bidding);
        verify(listener, times(2)).tableChanged(tableEventCaptor.capture());
        TableModelEvent event = tableEventCaptor.getAllValues().get(0);
        assertEquals(TableModelEvent.INSERT, event.getType());
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()));
        }
    }

    @Test
    public void notifiesListenersWhenAddingASniper() {
        ArgumentCaptor<TableModelEvent> tableEventCaptor = ArgumentCaptor.forClass(TableModelEvent.class);

        assertEquals(0, model.getRowCount());

        model.sniperAdded(sniper);

        verify(listener).tableChanged(tableEventCaptor.capture());
        assertEquals(1, model.getRowCount());
        assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID));
        TableModelEvent event = tableEventCaptor.getValue();
        assertEquals(model, event.getSource());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
        assertEquals(TableModelEvent.ALL_COLUMNS,event.getColumn());
        assertEquals(TableModelEvent.INSERT, event.getType());
    }

    @Test
    public void holdsSnipersInAdditionOrder() {
        AuctionSniper sniper2 = new AuctionSniper(new Item("item 1", 345), null);

        model.sniperAdded(sniper);
        model.sniperAdded(sniper2);

        assertEquals("item 0", cellValue(0, Column.ITEM_IDENTIFIER));
        assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER));
    }

    @Test
    public void updatesCorrectRowForSniper() {
        SniperSnapshot joining = sniper.getSnapshot();
        AuctionSniper sniper2 = new AuctionSniper(new Item("item 1", 345), null);
        SniperSnapshot bidding2 = sniper2.getSnapshot().bidding(22, 22);

        model.sniperAdded(sniper);
        model.sniperAdded(sniper2);
        model.sniperStateChanged((bidding2));

        assertRowMatchesSnapshot(0, joining);
        assertRowMatchesSnapshot(1, bidding2);
    }

    @Test
    public void throwsDefectIfNotExistingSniperForAnUpdate() {
        Assertions.assertThrows(Defect.class, () ->
                model.sniperStateChanged(
                        new SniperSnapshot("item 1", 123, 234, SniperState.WINNING)));
    }

    private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assertEquals(snapshot.itemId(), cellValue(row, Column.ITEM_IDENTIFIER));
        assertEquals(snapshot.lastPrice(), cellValue(row, Column.LAST_PRICE));
        assertEquals(snapshot.lastBid(), cellValue(row, Column.LAST_BID));
        assertEquals(textFor(snapshot.state()), cellValue(row, Column.SNIPER_STATE));
    }

    private Object cellValue(int row, Column column) {
        return model.getValueAt(row, column.ordinal());
    }
}

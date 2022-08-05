package auctionsniper;

import auctionsniper.ui.Column;
import auctionsniper.ui.SnipersTableModel;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static auctionsniper.ui.SnipersTableModel.textFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SnipersTableModelTest {
    @Mock
    private TableModelListener listener;
    private final SnipersTableModel model = new SnipersTableModel();

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
        model.sniperStateChanged(new SniperSnapshot("item id", 555, 666, SniperState.BIDDING));
        assertColumEquals(Column.ITEM_IDENTIFIER, "item id");
        assertColumEquals(Column.LAST_PRICE, 555);
        assertColumEquals(Column.LAST_BID, 666);
        assertColumEquals(Column.SNIPER_STATUS, textFor(SniperState.BIDDING));
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()));
        }
    }

    private void assertColumEquals(Column column, Object expected) {
        final int rowIndex = 0;
        final int columnIndex = column.ordinal();
        assertEquals(expected, model.getValueAt(rowIndex, columnIndex));
    }

    private Matcher<TableModelEvent> aRowChangedEvent() {
        return samePropertyValuesAs(new TableModelEvent(model, 0));
    }
}

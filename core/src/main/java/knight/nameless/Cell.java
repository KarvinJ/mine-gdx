package knight.nameless;

import com.badlogic.gdx.math.Rectangle;

public class Cell {
    public final int index;
    public final Rectangle bounds;
    public boolean isMined;
    public int mineCounter;

    public Cell(int cellIndex, Rectangle cellBounds) {
        index = cellIndex;
        bounds = cellBounds;
    }
}

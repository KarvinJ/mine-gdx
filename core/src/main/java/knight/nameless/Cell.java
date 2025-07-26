package knight.nameless;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;

public class Cell {
    public final int index;
    public final Rectangle bounds;
    public Texture sprite;
    public boolean isMined;
    public int mineCounter;
    public boolean isFlagged;

    public Cell(int cellIndex, Rectangle cellBounds) {
        index = cellIndex;
        bounds = cellBounds;
    }

    public void draw(Batch batch) {
        batch.draw(sprite, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void dispose(){

        if (sprite != null)
            sprite.dispose();
    }
}

package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Playground extends ApplicationAdapter {

    public final int screenWidth = 420;
    public final int screenHeight = 640;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    public ExtendViewport viewport;
    public OrthographicCamera camera;
    private final int TOTAL_ROWS = 9;
    private final int TOTAL_COLUMNS = 9;
    private final int HORIZONTAL_OFFSET = 7;
    private final int CELL_SIZE = 45;
    private final int VERTICAL_OFFSET = 100;
    private final int CELL_OFFSET = 2;
    private Cell[][] gameGrid;
    private Array<Integer> selectedCellsIndexes;
    private Array<Integer> mineCellsIndex;
    private Array<Integer> adjacentToMinesCellsIndex;

    @Override
    public void create() {

        camera = new OrthographicCamera();
        camera.position.set(screenWidth / 2f, screenHeight / 2f, 0);

        viewport = new ExtendViewport(screenWidth, screenHeight, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        gameGrid = new Cell[TOTAL_ROWS][TOTAL_COLUMNS];

        initializeGrid(gameGrid);

        selectedCellsIndexes = new Array<>();

        mineCellsIndex = new Array<>();

        adjacentToMinesCellsIndex = new Array<>();
    }

    private void initializeMineField(Array<Integer> mineCells, int firstSelectedIndex) {

        for (int i = 0; i < 10; i++) {

            var isAlreadyAdded = true;

            while (isAlreadyAdded) {

                int mineCellIndex = MathUtils.random(0, 81);

                isAlreadyAdded = mineCells.contains(mineCellIndex, true);

                if (!isAlreadyAdded) {

                    //the mine cannot be in the first index selected by the player.
                    if (mineCellIndex == firstSelectedIndex)
                        mineCellIndex++;

                    mineCells.add(mineCellIndex);
                }
            }
        }
    }

    private void initializeGrid(Cell[][] grid) {

        int index = 0;

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                Rectangle actualCellBounds = new Rectangle(
                    column * CELL_SIZE + HORIZONTAL_OFFSET,
                    row * CELL_SIZE + VERTICAL_OFFSET,
                    CELL_SIZE - CELL_OFFSET,
                    CELL_SIZE - CELL_OFFSET
                );

                grid[row][column] = new Cell(index, actualCellBounds);
                index++;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {

        ScreenUtils.clear(Color.BLACK);

//        batch.setProjectionMatrix(viewport.getCamera().combined);
//        batch.begin();
//
//        batch.end();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (Gdx.input.justTouched() && mouseBounds.overlaps(actualCell.bounds)) {

                    int selectedIndex = actualCell.index;
                    selectedCellsIndexes.add(actualCell.index);

                    if(selectedCellsIndexes.size == 1)
                        initializeMineField(mineCellsIndex, selectedIndex);
                }

                shapeRenderer.setColor(0.74f, 0.74f, 0.74f, 1);

                shapeRenderer.rect(
                    actualCell.bounds.x,
                    actualCell.bounds.y,
                    actualCell.bounds.width,
                    actualCell.bounds.height
                );

                if (selectedCellsIndexes.contains(actualCell.index, true)) {

                    shapeRenderer.setColor(Color.DARK_GRAY);

                    if (mineCellsIndex.contains(actualCell.index, true)) {

                        shapeRenderer.setColor(Color.RED);
                    }

                    if (adjacentToMinesCellsIndex.contains(actualCell.index, true)) {

                        shapeRenderer.setColor(Color.BLUE);
                    }

                    shapeRenderer.rect(
                        actualCell.bounds.x,
                        actualCell.bounds.y,
                        actualCell.bounds.width,
                        actualCell.bounds.height
                    );
                }
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}

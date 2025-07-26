package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
    private final int TOTAL_MINES = 10;
    private Cell[][] gameGrid;
    private Array<Integer> selectedCellsIndexes;
    private Array<Integer> adjacentToMinesCellsIndexes;
    private Array<Integer> mineCellsIndexes;
    private Texture mineTexture;
    private Texture flagTexture;
    private Array<Texture> tileNumberTextures;

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
        adjacentToMinesCellsIndexes = new Array<>();

        mineCellsIndexes = new Array<>();

        mineTexture = new Texture("img/TileMine.png");
        flagTexture = new Texture("img/TileFlag.png");

        tileNumberTextures = new Array<>();
        tileNumberTextures.add(
            new Texture("img/Tile1.png"),
            new Texture("img/Tile2.png"),
            new Texture("img/Tile3.png"),
            new Texture("img/Tile4.png")
        );

        tileNumberTextures.add(
            new Texture("img/Tile5.png"),
            new Texture("img/Tile6.png"),
            new Texture("img/Tile7.png"),
            new Texture("img/Tile8.png")
        );
    }

    private Array<Cell> getFlaggedCells() {

        var flaggedCells = new Array<Cell>();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isFlagged)
                    flaggedCells.add(actualCell);
            }
        }

        return flaggedCells;
    }

    private void initializeMineField(int firstSelectedIndex) {

        int gridMaxIndex = TOTAL_ROWS * TOTAL_COLUMNS;

        for (int i = 0; i < TOTAL_MINES; i++) {

            var isAlreadyAdded = true;

            while (isAlreadyAdded) {

                int mineCellIndex = MathUtils.random(0, gridMaxIndex);

                isAlreadyAdded = mineCellsIndexes.contains(mineCellIndex, true);

                if (!isAlreadyAdded) {

                    //the mine cannot be in the first index selected by the player.
                    if (mineCellIndex == firstSelectedIndex)
                        mineCellIndex++;

                    mineCellsIndexes.add(mineCellIndex);

                    for (int row = 0; row < TOTAL_ROWS; row++) {

                        for (int column = 0; column < TOTAL_COLUMNS; column++) {

                            var actualCell = gameGrid[row][column];

                            if (actualCell.index == mineCellIndex) {

                                actualCell.isMined = true;
                                actualCell.sprite = mineTexture;
                                break;
                            }
                        }
                    }
                }
            }
        }

        checkForAdjacentMines();
    }

    private void checkForAdjacentMines() {

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    continue;

                var previousColumn = column - 1;
                var nextColumn = column + 1;

                var previousRow = row - 1;
                var nextRow = row + 1;

                int mineCounter = 0;

                if (nextColumn < TOTAL_COLUMNS && gameGrid[row][nextColumn].isMined)
                    mineCounter++;

                if (previousColumn >= 0 && gameGrid[row][previousColumn].isMined)
                    mineCounter++;

                if (previousRow >= 0 && gameGrid[previousRow][column].isMined)
                    mineCounter++;

                if (nextRow < TOTAL_ROWS && gameGrid[nextRow][column].isMined)
                    mineCounter++;

                if (nextRow < TOTAL_ROWS && nextColumn < TOTAL_COLUMNS && gameGrid[nextRow][nextColumn].isMined)
                    mineCounter++;

                if (nextRow < TOTAL_ROWS && previousColumn >= 0 && gameGrid[nextRow][previousColumn].isMined)
                    mineCounter++;

                if (previousRow >= 0 && nextColumn < TOTAL_COLUMNS && gameGrid[previousRow][nextColumn].isMined)
                    mineCounter++;

                if (previousRow >= 0 && previousColumn >= 0 && gameGrid[previousRow][previousColumn].isMined)
                    mineCounter++;

                if (mineCounter > 0) {

                    actualCell.sprite = tileNumberTextures.get(mineCounter - 1);
                    actualCell.mineCounter = mineCounter;
                    adjacentToMinesCellsIndexes.add(actualCell.index);
                }
            }
        }
    }

    private void initializeGrid(Cell[][] grid) {

        int index = 0;

        int horizontalOffset = 7;
        int cellSize = 45;
        int verticalOffset = 100;
        int cellOffset = 2;

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                Rectangle actualCellBounds = new Rectangle(
                    column * cellSize + horizontalOffset,
                    row * cellSize + verticalOffset,
                    cellSize - cellOffset,
                    cellSize - cellOffset
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

    private void update() {

        if (Gdx.input.isKeyPressed(Input.Keys.R))
            resetGame();

        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (Gdx.input.justTouched() && mouseBounds.overlaps(actualCell.bounds)) {

                    var isAlreadyOpen = selectedCellsIndexes.contains(actualCell.index, true);

                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

                        if (actualCell.isFlagged) {

                            actualCell.isFlagged = false;
                            break;
                        }

                        if (mineCellsIndexes.size == 0)
                            initializeMineField(actualCell.index);

                        if (!isAlreadyOpen) {

                            selectedCellsIndexes.add(actualCell.index);
                            checkForCleanCells(actualCell, row, column);
                        }
                    }
                    else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && !isAlreadyOpen)
                        actualCell.isFlagged = !actualCell.isFlagged;
                }
            }
        }
    }

    @Override
    public void render() {

        update();

        ScreenUtils.clear(Color.BLACK);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                shapeRenderer.setColor(0.74f, 0.74f, 0.74f, 1);

                shapeRenderer.rect(
                    actualCell.bounds.x,
                    actualCell.bounds.y,
                    actualCell.bounds.width,
                    actualCell.bounds.height
                );

                if (selectedCellsIndexes.contains(actualCell.index, true)) {

                    shapeRenderer.setColor(Color.DARK_GRAY);

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

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (selectedCellsIndexes.contains(actualCell.index, true)) {

                    if (actualCell.isMined) {

                        actualCell.draw(batch);
//                        resetGame();
                    }

                    if (adjacentToMinesCellsIndexes.contains(actualCell.index, true))
                        actualCell.draw(batch);
                }

                for (var flaggedCell : getFlaggedCells()) {

                    batch.draw(
                        flagTexture,
                        flaggedCell.bounds.x,
                        flaggedCell.bounds.y,
                        flaggedCell.bounds.width,
                        flaggedCell.bounds.height
                    );
                }
            }
        }

        batch.end();
    }

    private void resetGame() {

        selectedCellsIndexes.clear();
        mineCellsIndexes.clear();
        adjacentToMinesCellsIndexes.clear();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                gameGrid[row][column].isFlagged = false;
                gameGrid[row][column].isMined = false;
                gameGrid[row][column].mineCounter = 0;
            }
        }
    }

    private void checkForCleanCells(Cell selectedCell, int selectedRow, int selectedColumn) {

        if (selectedCell.isMined || selectedCell.mineCounter > 0)
            return;

        for (int row = selectedRow; row >= 0; row--) {

            var previousRow = row + 1;

            if (previousRow < TOTAL_ROWS && gameGrid[previousRow][selectedColumn].isMined)
                break;

            for (int column = selectedColumn; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    break;

                if (!selectedCellsIndexes.contains(actualCell.index, true))
                    selectedCellsIndexes.add(gameGrid[row][column].index);
            }

            for (int column = selectedColumn; column >= 0; column--) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    break;

                if (!selectedCellsIndexes.contains(actualCell.index, true))
                    selectedCellsIndexes.add(gameGrid[row][column].index);
            }
        }

        for (int row = selectedRow; row < TOTAL_ROWS; row++) {

            var previousRow = row - 1;

            if (previousRow >= 0 && gameGrid[previousRow][selectedColumn].isMined)
                break;

            for (int column = selectedColumn; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    break;

                if (!selectedCellsIndexes.contains(actualCell.index, true))
                    selectedCellsIndexes.add(gameGrid[row][column].index);
            }

            for (int column = selectedColumn; column >= 0; column--) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    break;

                if (!selectedCellsIndexes.contains(actualCell.index, true))
                    selectedCellsIndexes.add(gameGrid[row][column].index);
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}

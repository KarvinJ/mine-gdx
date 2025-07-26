package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Playground extends ApplicationAdapter {

    public final int SCREEN_WIDTH = 420;
    public final int SCREEN_HEIGHT = 640;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    public ExtendViewport viewport;
    public OrthographicCamera camera;
    private final int TOTAL_ROWS = 9;
    private final int TOTAL_COLUMNS = 9;
    private final int TOTAL_MINES = 10;
    private boolean isGameOver = false;
    private Cell[][] gameGrid;
    private Array<Integer> selectedCellsIndexes;
    private Array<Integer> adjacentToMinesCellsIndexes;
    private Array<Integer> mineCellsIndexes;
    private Texture explodedMineTexture;
    private Texture mineTexture;
    private Texture emptyCellTexture;
    private Texture unknownCellTexture;
    private Texture flagTexture;
    private Texture smileyTexture;
    private Array<Texture> tileNumberTextures;
    private float time = 0;

    @Override
    public void create() {

        camera = new OrthographicCamera();
        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);

        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        gameGrid = new Cell[TOTAL_ROWS][TOTAL_COLUMNS];

        initializeGrid(gameGrid);

        selectedCellsIndexes = new Array<>();
        adjacentToMinesCellsIndexes = new Array<>();

        mineCellsIndexes = new Array<>();

        unknownCellTexture = new Texture("img/TileUnknown.png");
        emptyCellTexture = new Texture("img/TileEmpty.png");
        mineTexture = new Texture("img/TileMine.png");
        explodedMineTexture = new Texture("img/TileExploded.png");
        flagTexture = new Texture("img/TileFlag.png");
        smileyTexture = new Texture("img/TileSmiley.png");

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

    private Array<Cell> getMinedCells() {

        var minedCells = new Array<Cell>();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    minedCells.add(actualCell);
            }
        }

        return minedCells;
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

    private void update(Rectangle mouseBounds) {

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
                    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && !isAlreadyOpen)
                        actualCell.isFlagged = !actualCell.isFlagged;
                }
            }
        }
    }

    @Override
    public void render() {

        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        var smileyBounds = new Rectangle(SCREEN_WIDTH / 2f - 40 / 2f, SCREEN_HEIGHT - 80, 40, 40);

        if (Gdx.input.justTouched() && mouseBounds.overlaps(smileyBounds))
            resetGame();

        if (!isGameOver)
            update(mouseBounds);

        ScreenUtils.clear(Color.LIGHT_GRAY);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);

        var backgroundBonds = new Rectangle(-2, 92, SCREEN_WIDTH, 420);
        shapeRenderer.rect(
            backgroundBonds.x,
            backgroundBonds.y,
            backgroundBonds.width,
            backgroundBonds.height
        );

        shapeRenderer.end();

        var flaggedCells = getFlaggedCells();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        int totalFlags = TOTAL_MINES - flaggedCells.size;
        font.draw(batch, String.valueOf(totalFlags), 60, SCREEN_HEIGHT - 20);

        if (!isGameOver && !mineCellsIndexes.isEmpty())
            time += Gdx.graphics.getDeltaTime();

        font.draw(batch, String.valueOf((int) time), SCREEN_WIDTH - 60, SCREEN_HEIGHT - 20);

        batch.draw(
            smileyTexture,
            smileyBounds.x,
            smileyBounds.y,
            smileyBounds.width,
            smileyBounds.height
        );

        int selectedMineIndex = 0;

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                batch.draw(
                    unknownCellTexture,
                    actualCell.bounds.x,
                    actualCell.bounds.y,
                    actualCell.bounds.width,
                    actualCell.bounds.height
                );

                if (selectedCellsIndexes.contains(actualCell.index, true)) {

                    batch.draw(
                        emptyCellTexture,
                        actualCell.bounds.x,
                        actualCell.bounds.y,
                        actualCell.bounds.width,
                        actualCell.bounds.height
                    );

                    if (actualCell.isMined) {

                        selectedMineIndex = actualCell.index;

                        batch.draw(
                            explodedMineTexture,
                            actualCell.bounds.x,
                            actualCell.bounds.y,
                            actualCell.bounds.width,
                            actualCell.bounds.height
                        );

                        isGameOver = true;
                    }

                    if (adjacentToMinesCellsIndexes.contains(actualCell.index, true))
                        actualCell.draw(batch);
                }

                if (isGameOver) {

                    for (var minedCell : getMinedCells()) {

                        if (minedCell.index == selectedMineIndex)
                            continue;

                        batch.draw(
                            mineTexture,
                            minedCell.bounds.x,
                            minedCell.bounds.y,
                            minedCell.bounds.width,
                            minedCell.bounds.height
                        );
                    }
                }

                for (var flaggedCell : flaggedCells) {

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

        isGameOver = false;
        selectedCellsIndexes.clear();
        mineCellsIndexes.clear();
        adjacentToMinesCellsIndexes.clear();
        time = 0;

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
        shapeRenderer.dispose();
        font.dispose();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {
                gameGrid[row][column].dispose();
            }
        }
    }
}

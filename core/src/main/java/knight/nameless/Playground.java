package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private Texture wrongFlagTexture;
    private Texture smileyTexture;
    private Array<Texture> tileNumberTextures;
    private TextureRegion[] scoreNumbers;
    private float time = 998;

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
        wrongFlagTexture = new Texture("img/TileFlagWrong.png");
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

        scoreNumbers = loadTextureSprite();
    }

    private TextureRegion[] loadTextureSprite() {

        Texture textureToSplit = new Texture("img/numbers.png");

        return TextureRegion.split(
            textureToSplit, textureToSplit.getWidth() / 10,
            textureToSplit.getHeight()
        )[0];
    }

    private void drawNumbers(SpriteBatch batch, int number, float positionX) {

        final float width = 48;
        final float height = 64;
        final float positionY = SCREEN_HEIGHT - 90;
        var spaceBetweenNumbers = scoreNumbers[0].getRegionWidth() * 2 - 10;

        if (number > 999) {

            batch.draw(scoreNumbers[9], positionX, positionY, width, height);
            batch.draw(scoreNumbers[9], positionX + spaceBetweenNumbers, positionY, width, height);
            batch.draw(scoreNumbers[9], positionX + spaceBetweenNumbers * 2, positionY, width, height);
        }

        else if (number < 10) {

            batch.draw(scoreNumbers[0], positionX, positionY, width, height);
            batch.draw(scoreNumbers[0], positionX + spaceBetweenNumbers, positionY, width, height);

            if (number >= 0)
                batch.draw(scoreNumbers[number], positionX + spaceBetweenNumbers * 2, positionY, width, height);
            else
                batch.draw(scoreNumbers[0], positionX + spaceBetweenNumbers * 2, positionY, width, height);
        }
        else if (number < 100) {

            int tens = number / 10;
            int units = number % 10;

            batch.draw(scoreNumbers[0], positionX, positionY, width, height);
            batch.draw(scoreNumbers[tens], positionX + spaceBetweenNumbers, positionY, width, height);
            batch.draw(scoreNumbers[units], positionX + spaceBetweenNumbers * 2, positionY, width, height);
        }
        else {

            int hundred = number / 100;
            int hundredUnits = number % 100;

            batch.draw(scoreNumbers[hundred], positionX, positionY, width, height);

            if (hundredUnits < 10) {

                batch.draw(scoreNumbers[0], positionX + spaceBetweenNumbers, positionY, width, height);
                batch.draw(scoreNumbers[hundredUnits], positionX + spaceBetweenNumbers * 2, positionY, width, height);
            }
            else {

                int hundredTens = hundredUnits / 10;
                int hundredUnits2 = hundredUnits % 10;

                batch.draw(scoreNumbers[hundredTens], positionX + spaceBetweenNumbers, positionY, width, height);
                batch.draw(scoreNumbers[hundredUnits2], positionX + spaceBetweenNumbers * 2, positionY, width, height);
            }
        }
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

        int gridSize = TOTAL_ROWS * TOTAL_COLUMNS;

        final int TOTAL_MINES = 10;
        for (int i = 0; i < TOTAL_MINES; i++) {

            var isAlreadyAdded = true;

            while (isAlreadyAdded) {

                int mineCellIndex = MathUtils.random(0, gridSize);

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

                                actualCell.cellValue = 9;
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

                    actualCell.cellValue = mineCounter;
                    actualCell.sprite = tileNumberTextures.get(mineCounter - 1);
                    adjacentToMinesCellsIndexes.add(actualCell.index);
                }
            }
        }
    }

    private void initializeGrid(Cell[][] grid) {

        int index = 0;

        int horizontalOffset = 9;
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
                grid[row][column].cellValue = 0;
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

        var backgroundBonds = new Rectangle(-2, 92, SCREEN_WIDTH + 2, 420);
        shapeRenderer.rect(
            backgroundBonds.x,
            backgroundBonds.y,
            backgroundBonds.width,
            backgroundBonds.height
        );

        shapeRenderer.end();

        var flaggedCells = getFlaggedCells();
        var minedCells = getMinedCells();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        int totalFlags = minedCells.size - flaggedCells.size;

        drawNumbers(batch, totalFlags, 40);

        if (!isGameOver && !mineCellsIndexes.isEmpty())
            time += Gdx.graphics.getDeltaTime();

        drawNumbers(batch, (int) time, SCREEN_WIDTH - 150);

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
                        font.draw(batch, "You Lose", SCREEN_WIDTH / 2f - 25, SCREEN_HEIGHT - 20);
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

                    renderGameOverFlags(flaggedCells, minedCells);
                } else {

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

                int foundMines = 0;

                for (var flaggedCell : flaggedCells) {

                    for (var minedCell : minedCells) {

                        if (flaggedCell.index == minedCell.index) {
                            foundMines++;
                            break;
                        }
                    }
                }

                //also player can win without revealing all cells. Need to fix both of these issues.
                //for some reason sometimes there is only 9 mines instead of 10
                if (!minedCells.isEmpty() && totalFlags == 0 && foundMines == minedCells.size) {

                    isGameOver = true;
                    font.draw(batch, "You Won", SCREEN_WIDTH / 2f - 25, SCREEN_HEIGHT - 20);
                }
            }
        }

        batch.end();
    }

    private void renderGameOverFlags(Array<Cell> flaggedCells, Array<Cell> minedCells) {

        for (var flaggedCell : flaggedCells) {

            var wrongCellFlagged = true;

            for (var minedCell : minedCells) {

                if (flaggedCell.index == minedCell.index) {

                    wrongCellFlagged = false;
                    break;
                }
            }

            if (wrongCellFlagged) {

                batch.draw(
                    wrongFlagTexture,
                    flaggedCell.bounds.x,
                    flaggedCell.bounds.y,
                    flaggedCell.bounds.width,
                    flaggedCell.bounds.height
                );
            } else {

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
                gameGrid[row][column].cellValue = 0;
            }
        }
    }

    private void checkForCleanCells(Cell selectedCell, int selectedRow, int selectedColumn) {

        //I don't need to evaluate mine cells (9) and adjacent to mine cells
        if (selectedCell.cellValue > 0)
            return;

        var result = floodFill(gameGrid, selectedRow, selectedColumn);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = result[row][column];

                if (actualCell.isMined)
                    continue;

                if (actualCell.cellValue == 10) {

                    selectedCellsIndexes.add(actualCell.index);

                    for (var adjacentIndex : adjacentToMinesCellsIndexes) {

                        var previousColumn = column - 1;
                        var nextColumn = column + 1;

                        var previousRow = row - 1;
                        var nextRow = row + 1;

                        if (nextColumn < TOTAL_COLUMNS && gameGrid[row][nextColumn].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[row][nextColumn].index);

                        if (previousColumn >= 0 && gameGrid[row][previousColumn].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[row][previousColumn].index);

                        if (previousRow >= 0 && gameGrid[previousRow][column].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[previousRow][column].index);

                        if (nextRow < TOTAL_ROWS && nextColumn < TOTAL_COLUMNS && gameGrid[nextRow][nextColumn].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[nextRow][nextColumn].index);

                        if (nextRow < TOTAL_ROWS && previousColumn >= 0 && gameGrid[nextRow][previousColumn].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[nextRow][previousColumn].index);

                        if (previousRow >= 0 && nextColumn < TOTAL_COLUMNS && gameGrid[previousRow][nextColumn].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[previousRow][nextColumn].index);

                        if (previousRow >= 0 && previousColumn >= 0 && gameGrid[previousRow][previousColumn].index == adjacentIndex)
                            selectedCellsIndexes.add(gameGrid[previousRow][previousColumn].index);
                    }
                }
            }
        }
    }

    private Cell[][] floodFill(Cell[][] image, int selectedRow, int selectedColumn) {

        //default value to change my empty spaces.
        final int newValue = 10;

        // If the starting pixel already has the new color, no need
        // to process
        if (image[selectedRow][selectedColumn].cellValue == newValue)
            return image;

        // Call DFS with the original color of the starting pixel
        depthFirstSearch(image, selectedRow, selectedColumn, image[selectedRow][selectedColumn].cellValue, newValue);

        // Return the updated image
        return image;
    }

    //    In Depth First Search (or DFS) for a graph, we traverse all adjacent vertices one by one.
    private void depthFirstSearch(Cell[][] image, int x, int y, int oldValue, int newValue) {

        // Base case: check for out-of-bound indices or mismatched color
        if (x < 0 || x >= image.length || y < 0 || y >= image[0].length || image[x][y].cellValue != oldValue)
            return; // Backtrack if invalid

        // Change the color of the current pixel
        image[x][y].cellValue = newValue;

        // Recursively call DFS in all four directions
        depthFirstSearch(image, x + 1, y, oldValue, newValue);
        depthFirstSearch(image, x - 1, y, oldValue, newValue);
        depthFirstSearch(image, x, y + 1, oldValue, newValue);
        depthFirstSearch(image, x, y - 1, oldValue, newValue);
    }

    @Override
    public void dispose() {

        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        mineTexture.dispose();
        explodedMineTexture.dispose();
        smileyTexture.dispose();
        emptyCellTexture.dispose();
        flagTexture.dispose();
        wrongFlagTexture.dispose();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {
                gameGrid[row][column].dispose();
            }
        }
    }
}

package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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

public class Playground extends ApplicationAdapter implements InputProcessor {

    public final int SCREEN_WIDTH = 420;
    public final int SCREEN_HEIGHT = 720;
    private int TOTAL_ROWS = 9;
    private final int TOTAL_COLUMNS = 9;
    private int TOTAL_MINES = 10;
    private float time = 0;
    private boolean isGameOver = false;
    private boolean youWin = false;
    private boolean shouldCheckForMines = true;
    private boolean isHardMode = false;
    private Cell[][] gameGrid;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    public ExtendViewport viewport;
    public OrthographicCamera camera;
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
    private boolean touchRelease = false;

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

        for (int i = 1; i < 9; i++) {

            tileNumberTextures.add(new Texture("img/Tile" + i + ".png"));
        }

        scoreNumbers = loadNumbersTextureRegion();

        Gdx.input.setInputProcessor(this);
    }

    private TextureRegion[] loadNumbersTextureRegion() {

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
        } else if (number < 10) {

            batch.draw(scoreNumbers[0], positionX, positionY, width, height);
            batch.draw(scoreNumbers[0], positionX + spaceBetweenNumbers, positionY, width, height);

            if (number >= 0)
                batch.draw(scoreNumbers[number], positionX + spaceBetweenNumbers * 2, positionY, width, height);
            else
                batch.draw(scoreNumbers[0], positionX + spaceBetweenNumbers * 2, positionY, width, height);
        } else if (number < 100) {

            int tens = number / 10;
            int units = number % 10;

            batch.draw(scoreNumbers[0], positionX, positionY, width, height);
            batch.draw(scoreNumbers[tens], positionX + spaceBetweenNumbers, positionY, width, height);
            batch.draw(scoreNumbers[units], positionX + spaceBetweenNumbers * 2, positionY, width, height);
        } else {

            int hundred = number / 100;
            int hundredUnits = number % 100;

            batch.draw(scoreNumbers[hundred], positionX, positionY, width, height);

            if (hundredUnits < 10) {

                batch.draw(scoreNumbers[0], positionX + spaceBetweenNumbers, positionY, width, height);
                batch.draw(scoreNumbers[hundredUnits], positionX + spaceBetweenNumbers * 2, positionY, width, height);
            } else {

                int hundredTens = hundredUnits / 10;
                int hundredUnits2 = hundredUnits % 10;

                batch.draw(scoreNumbers[hundredTens], positionX + spaceBetweenNumbers, positionY, width, height);
                batch.draw(scoreNumbers[hundredUnits2], positionX + spaceBetweenNumbers * 2, positionY, width, height);
            }
        }
    }

    private Array<Cell> getOpenCells() {

        var openCells = new Array<Cell>();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isOpen)
                    openCells.add(actualCell);
            }
        }

        return openCells;
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

        //grid size is 81, but the grid index is 80
        int gridSize = TOTAL_ROWS * TOTAL_COLUMNS - 1;

        int addedMines = 0;
        while (addedMines < TOTAL_MINES) {

            var isAlreadyAdded = true;

            while (isAlreadyAdded) {

                int mineCellIndex = MathUtils.random(0, gridSize);

                isAlreadyAdded = mineCellsIndexes.contains(mineCellIndex, true);

                //the mine cannot be in the first index selected by the player.
                if (!isAlreadyAdded && mineCellIndex != firstSelectedIndex) {

                    mineCellsIndexes.add(mineCellIndex);

                    for (int row = 0; row < TOTAL_ROWS; row++) {

                        for (int column = 0; column < TOTAL_COLUMNS; column++) {

                            var actualCell = gameGrid[row][column];

                            if (actualCell.index == mineCellIndex) {

                                actualCell.cellValue = 9;
                                actualCell.isMined = true;
                                addedMines++;
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
        int verticalOffset = isHardMode ? 2 : 180;
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

                if (mouseBounds.overlaps(actualCell.bounds)) {

                    if ((Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) || (Gdx.input.justTouched() && !shouldCheckForMines))) {

                        if (!actualCell.isOpen)
                            actualCell.isFlagged = !actualCell.isFlagged;
                    }

                    else if (touchRelease) {

                        if (actualCell.isFlagged) {

                            actualCell.isFlagged = false;
                            touchRelease = false;
                            break;
                        }

                        if (mineCellsIndexes.size == 0)
                            initializeMineField(actualCell.index);

                        if (!actualCell.isOpen) {

                            actualCell.isOpen = true;
                            checkForCleanCells(actualCell, row, column);
                        }

                        touchRelease = false;
                    }
                }
            }
        }
    }

    @Override
    public void render() {

        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        if (!isGameOver)
            update(mouseBounds);

        ScreenUtils.clear(Color.LIGHT_GRAY);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);

        var yValue = isHardMode ? 0 : 172;
        var height = isHardMode ? 592 : 420;
        var backgroundBounds = new Rectangle(-2, yValue, SCREEN_WIDTH + 2, height);

        shapeRenderer.rect(
            backgroundBounds.x,
            backgroundBounds.y,
            backgroundBounds.width,
            backgroundBounds.height
        );

        shapeRenderer.end();

        var flaggedCells = getFlaggedCells();
        var minedCells = getMinedCells();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        manageUIElements(mouseBounds, flaggedCells.size);

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

                if (mouseBounds.overlaps(actualCell.bounds)) {

                    batch.draw(
                        emptyCellTexture,
                        actualCell.bounds.x,
                        actualCell.bounds.y,
                        actualCell.bounds.width,
                        actualCell.bounds.height
                    );
                }

                if (actualCell.isOpen) {

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

                    //draw adjacent mines cells cell with value 1 to 8
                    if (actualCell.cellValue > 0 && actualCell.cellValue < 9)
                        actualCell.draw(batch);
                }

                if (isGameOver) {

                    if (youWin) {

                        for (var minedCell : minedCells) {

                            batch.draw(
                                flagTexture,
                                minedCell.bounds.x,
                                minedCell.bounds.y,
                                minedCell.bounds.width,
                                minedCell.bounds.height
                            );
                        }
                    } else {

                        for (var minedCell : minedCells) {

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

                    renderGameOverFlags(flaggedCells, minedCells);

                    if (youWin)
                        font.draw(batch, "You Won", SCREEN_WIDTH / 2f - 30, SCREEN_HEIGHT - 20);
                    else
                        font.draw(batch, "You Lose", SCREEN_WIDTH / 2f - 30, SCREEN_HEIGHT - 20);

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

                var totalOpenCells = getOpenCells().size + TOTAL_MINES;
                int gridSize = TOTAL_ROWS * TOTAL_COLUMNS;

                if (!minedCells.isEmpty() && totalOpenCells == gridSize) {

                    youWin = true;
                    isGameOver = true;
                }
            }
        }

        batch.end();
    }

    private void manageUIElements(Rectangle mouseBounds, int totalFlaggedCells) {

        int totalFlags = TOTAL_MINES - totalFlaggedCells;

        drawNumbers(batch, totalFlags, 40);

        if (!isGameOver && !mineCellsIndexes.isEmpty())
            time += Gdx.graphics.getDeltaTime();

        drawNumbers(batch, (int) time, SCREEN_WIDTH - 150);

        var smileyBounds = new Rectangle(SCREEN_WIDTH / 2f - 50 / 2f, SCREEN_HEIGHT - 85, 50, 50);
        var stateBounds = new Rectangle(SCREEN_WIDTH - 35, SCREEN_HEIGHT - 125, 35, 35);
        var difficultyBounds = new Rectangle(0, SCREEN_HEIGHT - 125, 35, 35);

        if (Gdx.input.justTouched() && mouseBounds.overlaps(difficultyBounds)) {

            isHardMode = !isHardMode;
            resetGame();
        }

        if (Gdx.input.justTouched() && mouseBounds.overlaps(stateBounds))
            shouldCheckForMines = !shouldCheckForMines;

        if (Gdx.input.justTouched() && mouseBounds.overlaps(smileyBounds))
            resetGame();

        if (isHardMode) {

            batch.draw(
                tileNumberTextures.get(1),
                difficultyBounds.x,
                difficultyBounds.y,
                difficultyBounds.width,
                difficultyBounds.height
            );
        } else {

            batch.draw(
                tileNumberTextures.get(0),
                difficultyBounds.x,
                difficultyBounds.y,
                difficultyBounds.width,
                difficultyBounds.height
            );
        }

        if (shouldCheckForMines) {

            batch.draw(
                mineTexture,
                stateBounds.x,
                stateBounds.y,
                stateBounds.width,
                stateBounds.height
            );
        } else {

            batch.draw(
                flagTexture,
                stateBounds.x,
                stateBounds.y,
                stateBounds.width,
                stateBounds.height
            );
        }

        batch.draw(
            smileyTexture,
            smileyBounds.x,
            smileyBounds.y,
            smileyBounds.width,
            smileyBounds.height
        );
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

        youWin = false;
        isGameOver = false;
        mineCellsIndexes.clear();
        adjacentToMinesCellsIndexes.clear();
        time = 0;

        if (isHardMode) {

            TOTAL_MINES = 20;
            TOTAL_ROWS = 13;
        }
        else {

            TOTAL_MINES = 10;
            TOTAL_ROWS = 9;
        }

        gameGrid = new Cell[TOTAL_ROWS][TOTAL_COLUMNS];
        initializeGrid(gameGrid);
    }

    private void checkForCleanCells(Cell selectedCell, int selectedRow, int selectedColumn) {

        //I don't need to evaluate mine cells (9) and adjacent to mine cells, just the empty cells (0)
        if (selectedCell.cellValue > 0)
            return;

        var result = floodFill(gameGrid, selectedRow, selectedColumn);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                var actualCell = result[row][column];

                if (actualCell.isMined || actualCell.isOpen)
                    continue;

                if (actualCell.cellValue == 10) {

                    actualCell.isOpen = true;

                    for (var adjacentIndex : adjacentToMinesCellsIndexes) {

                        var previousColumn = column - 1;
                        var nextColumn = column + 1;

                        var previousRow = row - 1;
                        var nextRow = row + 1;

                        if (nextColumn < TOTAL_COLUMNS && gameGrid[row][nextColumn].index == adjacentIndex)
                            gameGrid[row][nextColumn].isOpen = true;

                        if (previousColumn >= 0 && gameGrid[row][previousColumn].index == adjacentIndex)
                            gameGrid[row][previousColumn].isOpen = true;

                        if (previousRow >= 0 && gameGrid[previousRow][column].index == adjacentIndex)
                            gameGrid[previousRow][column].isOpen = true;

                        if (nextRow < TOTAL_ROWS && nextColumn < TOTAL_COLUMNS && gameGrid[nextRow][nextColumn].index == adjacentIndex)
                            gameGrid[nextRow][nextColumn].isOpen = true;

                        if (nextRow < TOTAL_ROWS && previousColumn >= 0 && gameGrid[nextRow][previousColumn].index == adjacentIndex)
                            gameGrid[nextRow][previousColumn].isOpen = true;

                        if (previousRow >= 0 && nextColumn < TOTAL_COLUMNS && gameGrid[previousRow][nextColumn].index == adjacentIndex)
                            gameGrid[previousRow][nextColumn].isOpen = true;

                        if (previousRow >= 0 && previousColumn >= 0 && gameGrid[previousRow][previousColumn].index == adjacentIndex)
                            gameGrid[previousRow][previousColumn].isOpen = true;
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

        for (var tileNumberTexture : tileNumberTextures)
            tileNumberTexture.dispose();

        tileNumberTextures.clear();

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {
                gameGrid[row][column].dispose();
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        touchRelease = false;

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        touchRelease = button == Input.Buttons.LEFT && shouldCheckForMines;

        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    //this metho always have the mouse position
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}

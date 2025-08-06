package knight.nameless;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Playground extends ApplicationAdapter {

    public final int SCREEN_WIDTH = 420;
    public final int SCREEN_HEIGHT = 720;
    private float time = 0;
    private boolean isGameOver = false;
    private boolean youWin = false;
    private MineSweeper mineSweeper;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    public ExtendViewport viewport;
    public OrthographicCamera camera;
    private Texture explodedMineTexture;
    private Texture mineTexture;
    private Texture emptyCellTexture;
    private Texture unknownCellTexture;
    private Texture flagTexture;
    private Texture wrongFlagTexture;
    private Texture smileyTexture;
    private TextureRegion[] scoreNumbers;
    private Sound boomSound;
    private Sound clickSound;
    private Sound tapSound;
    private boolean theGameHasBeenReset = false;
    private boolean isAndroid = false;
    private MyInputProcessor myInputProcessor;

    @Override
    public void create() {

        if (Gdx.app.getType() == Application.ApplicationType.Android)
            isAndroid = true;

        camera = new OrthographicCamera();
        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        mineSweeper = new MineSweeper();

        unknownCellTexture = new Texture("img/TileUnknown.png");
        emptyCellTexture = new Texture("img/TileEmpty.png");
        mineTexture = new Texture("img/TileMine.png");
        explodedMineTexture = new Texture("img/TileExploded.png");
        flagTexture = new Texture("img/TileFlag.png");
        wrongFlagTexture = new Texture("img/TileFlagWrong.png");
        smileyTexture = new Texture("img/TileSmiley.png");

        scoreNumbers = loadNumbersTextureRegion();

        boomSound = Gdx.audio.newSound(Gdx.files.internal("sounds/boom.wav"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("sounds/click.wav"));
        tapSound = Gdx.audio.newSound(Gdx.files.internal("sounds/tap.wav"));

        myInputProcessor = new MyInputProcessor();
        Gdx.input.setInputProcessor(myInputProcessor);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        final float positionY = SCREEN_HEIGHT - 70;
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

    private void update(Rectangle mouseBounds) {

        for (int row = 0; row < mineSweeper.totalRows; row++) {

            for (int column = 0; column < mineSweeper.totalColumns; column++) {

                var actualCell = mineSweeper.gameGrid[row][column];

                if (mouseBounds.overlaps(actualCell.bounds)) {

                    //need to do this to avoid the first click bug when resetting the game
                    if (theGameHasBeenReset) {

                        theGameHasBeenReset = false;
                        myInputProcessor.touchRelease = false;
                        return;
                    }

                    if ((Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) || (Gdx.input.justTouched() && !myInputProcessor.shouldCheckForMines))) {

                        tapSound.play();

                        if (!actualCell.isOpen)
                            actualCell.isFlagged = !actualCell.isFlagged;

                    } else if (myInputProcessor.touchRelease) {

                        clickSound.play();

                        if (actualCell.isOpen)
                            mineSweeper.checkAdjacentCellsToOpenByRowAndColumn(row, column);

                        if (actualCell.isFlagged) {

                            actualCell.isFlagged = false;
                            myInputProcessor.touchRelease = false;
                            break;
                        }

                        if (mineSweeper.mineCellsIndexes.size == 0)
                            mineSweeper.initializeMineField(actualCell.index);

                        if (!actualCell.isOpen) {

                            actualCell.isOpen = true;
                            mineSweeper.checkForCleanCells(actualCell, row, column);
                        }

                        if (actualCell.isMined)
                            boomSound.play();

                        myInputProcessor.touchRelease = false;
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

        var yValue = mineSweeper.isHardMode ? 0 : 212;
        var height = mineSweeper.isHardMode ? 628 : 420;
        var backgroundBounds = new Rectangle(-2, yValue, SCREEN_WIDTH + 2, height);

        shapeRenderer.rect(
            backgroundBounds.x,
            backgroundBounds.y,
            backgroundBounds.width,
            backgroundBounds.height
        );

        shapeRenderer.end();

        var flaggedCells = mineSweeper.getFlaggedCells();
        var minedCells = mineSweeper.getMinedCells();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        manageUIElements(mouseBounds, flaggedCells.size, minedCells.size);

        int selectedMineIndex = 0;

        for (int row = 0; row < mineSweeper.totalRows; row++) {

            for (int column = 0; column < mineSweeper.totalColumns; column++) {

                var actualCell = mineSweeper.gameGrid[row][column];

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

                    renderGameOverTextures(flaggedCells, minedCells);

                    if (youWin)
                        font.draw(batch, "You Win", SCREEN_WIDTH / 2f - 30, SCREEN_HEIGHT - 4);
                    else
                        font.draw(batch, "You Lose", SCREEN_WIDTH / 2f - 30, SCREEN_HEIGHT - 4);

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

                var totalOpenCells = mineSweeper.getOpenCells().size + mineSweeper.getMinedCells().size;
                int gridSize = mineSweeper.totalRows * mineSweeper.totalColumns;

                if (!minedCells.isEmpty() && totalOpenCells == gridSize) {

                    youWin = true;
                    isGameOver = true;
                }
            }
        }

        batch.end();
    }

    private void manageUIElements(Rectangle mouseBounds, int totalFlaggedCells, int totalMines) {

        int totalFlags = totalMines - totalFlaggedCells;

        drawNumbers(batch, totalFlags, 40);

        if (!isGameOver && !mineSweeper.mineCellsIndexes.isEmpty())
            time += Gdx.graphics.getDeltaTime();

        drawNumbers(batch, (int) time, SCREEN_WIDTH - 150);

        var smileyBounds = new Rectangle(SCREEN_WIDTH / 2f - 45 / 2f, SCREEN_HEIGHT - 60, 45, 45);
        var stateBounds = new Rectangle(SCREEN_WIDTH - 35, SCREEN_HEIGHT - 35, 35, 35);
        var difficultyBounds = new Rectangle(0, SCREEN_HEIGHT - 35, 35, 35);

        if (Gdx.input.justTouched() && mouseBounds.overlaps(difficultyBounds)) {

            mineSweeper.isHardMode = !mineSweeper.isHardMode;
            resetGame();
        }

        if (isAndroid && Gdx.input.justTouched() && mouseBounds.overlaps(stateBounds))
            myInputProcessor.shouldCheckForMines = !myInputProcessor.shouldCheckForMines;

        if (Gdx.input.justTouched() && mouseBounds.overlaps(smileyBounds))
            resetGame();

        if (mineSweeper.isHardMode) {

            batch.draw(
                mineSweeper.tileNumberTextures.get(1),
                difficultyBounds.x,
                difficultyBounds.y,
                difficultyBounds.width,
                difficultyBounds.height
            );
        } else {

            batch.draw(
                mineSweeper.tileNumberTextures.get(0),
                difficultyBounds.x,
                difficultyBounds.y,
                difficultyBounds.width,
                difficultyBounds.height
            );
        }


        if (isAndroid) {

            if (myInputProcessor.shouldCheckForMines) {

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
        }

        batch.draw(
            smileyTexture,
            smileyBounds.x,
            smileyBounds.y,
            smileyBounds.width,
            smileyBounds.height
        );
    }

    private void renderGameOverTextures(Array<Cell> flaggedCells, Array<Cell> minedCells) {

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

        theGameHasBeenReset = true;
        youWin = false;
        isGameOver = false;
        mineSweeper.mineCellsIndexes.clear();
        mineSweeper.adjacentToMinesCellsIndexes.clear();
        time = 0;

        if (mineSweeper.isHardMode) {

            mineSweeper.totalMines = 27;
            mineSweeper.totalRows = 16;
            mineSweeper.totalColumns = 11;
        } else {

            mineSweeper.totalMines = 10;
            mineSweeper.totalRows = 9;
            mineSweeper.totalColumns = 9;
        }

        mineSweeper.gameGrid = new Cell[mineSweeper.totalRows][mineSweeper.totalColumns];
        mineSweeper.initializeGrid(mineSweeper.gameGrid);
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
        tapSound.dispose();
        clickSound.dispose();
        boomSound.dispose();
        mineSweeper.dispose();
    }
}

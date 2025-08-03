package knight.nameless;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MineSweeper {

    public Cell[][] gameGrid;
    public int totalRows = 9;
    public int totalColumns = 9;
    public int totalMines = 10;
    public final Array<Integer> adjacentToMinesCellsIndexes;
    public final Array<Integer> mineCellsIndexes;
    public final Array<Texture> tileNumberTextures;
    public boolean isHardMode = false;

    public MineSweeper() {

        gameGrid = new Cell[totalRows][totalColumns];

        initializeGrid(gameGrid);

        tileNumberTextures = new Array<>();

        for (int i = 1; i < 9; i++) {

            tileNumberTextures.add(new Texture("img/Tile" + i + ".png"));
        }
        adjacentToMinesCellsIndexes = new Array<>();
        mineCellsIndexes = new Array<>();
    }

    public void initializeGrid(Cell[][] grid) {

        int index = 0;

        int horizontalOffset = isHardMode ? 2 : 9;
        int cellSize = isHardMode ? 38 : 45;
        int verticalOffset = isHardMode ? 10 : 220;
        int cellOffset = 2;

        for (int row = 0; row < totalRows; row++) {

            for (int column = 0; column < totalColumns; column++) {

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

    public void initializeMineField(int firstSelectedIndex) {

        int gridSize = totalRows * totalColumns - 1;

        //this algorithm fails for the hard mode 27 mines, sometimes just put 25 or 26
        int addedMines = 0;
        while (addedMines < totalMines) {

            var isAlreadyAdded = true;

            while (isAlreadyAdded) {

                int mineCellIndex = MathUtils.random(0, gridSize);

                isAlreadyAdded = mineCellsIndexes.contains(mineCellIndex, true);

                //the mine cannot be in the first index selected by the player.
                if (!isAlreadyAdded && mineCellIndex != firstSelectedIndex) {

                    mineCellsIndexes.add(mineCellIndex);

                    for (int row = 0; row < totalRows; row++) {

                        for (int column = 0; column < totalColumns; column++) {

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

        setAdjacentToMinesCells();
    }

    private void setAdjacentToMinesCells() {

        for (int row = 0; row < totalRows; row++) {

            for (int column = 0; column < totalColumns; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    continue;

                var previousColumn = column - 1;
                var nextColumn = column + 1;

                var previousRow = row - 1;
                var nextRow = row + 1;

                int mineCounter = 0;

                if (nextColumn < totalColumns && gameGrid[row][nextColumn].isMined)
                    mineCounter++;

                if (previousColumn >= 0 && gameGrid[row][previousColumn].isMined)
                    mineCounter++;

                if (previousRow >= 0 && gameGrid[previousRow][column].isMined)
                    mineCounter++;

                if (nextRow < totalRows && gameGrid[nextRow][column].isMined)
                    mineCounter++;

                if (nextRow < totalRows && nextColumn < totalColumns && gameGrid[nextRow][nextColumn].isMined)
                    mineCounter++;

                if (nextRow < totalRows && previousColumn >= 0 && gameGrid[nextRow][previousColumn].isMined)
                    mineCounter++;

                if (previousRow >= 0 && nextColumn < totalColumns && gameGrid[previousRow][nextColumn].isMined)
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

    public Array<Cell> getOpenCells() {

        var openCells = new Array<Cell>();

        for (int row = 0; row < totalRows; row++) {

            for (int column = 0; column < totalColumns; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isOpen)
                    openCells.add(actualCell);
            }
        }

        return openCells;
    }

    public Array<Cell> getFlaggedCells() {

        var flaggedCells = new Array<Cell>();

        for (int row = 0; row < totalRows; row++) {

            for (int column = 0; column < totalColumns; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isFlagged)
                    flaggedCells.add(actualCell);
            }
        }

        return flaggedCells;
    }

    public Array<Cell> getMinedCells() {

        var minedCells = new Array<Cell>();

        for (int row = 0; row < totalRows; row++) {

            for (int column = 0; column < totalColumns; column++) {

                var actualCell = gameGrid[row][column];

                if (actualCell.isMined)
                    minedCells.add(actualCell);
            }
        }

        return minedCells;
    }

    public void dispose () {

        for (var tileNumberTexture : tileNumberTextures)
            tileNumberTexture.dispose();

        tileNumberTextures.clear();
    }
}

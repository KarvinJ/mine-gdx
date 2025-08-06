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

    public void checkAdjacentCellsToOpenByRowAndColumn(int row, int column) {

        var previousColumn = column - 1;
        var nextColumn = column + 1;

        var previousRow = row - 1;
        var nextRow = row + 1;

        var adjacentCells = new Array<Cell>();

        if (nextColumn < totalColumns)
            adjacentCells.add(gameGrid[row][nextColumn]);

        if (previousColumn >= 0)
            adjacentCells.add(gameGrid[row][previousColumn]);

        if (previousRow >= 0)
            adjacentCells.add(gameGrid[previousRow][column]);

        if (nextRow < totalRows)
            adjacentCells.add(gameGrid[nextRow][column]);

        if (nextRow < totalRows && nextColumn < totalColumns)
            adjacentCells.add(gameGrid[nextRow][nextColumn]);

        if (nextRow < totalRows && previousColumn >= 0)
            adjacentCells.add(gameGrid[nextRow][previousColumn]);

        if (previousRow >= 0 && nextColumn < totalColumns)
            adjacentCells.add(gameGrid[previousRow][nextColumn]);

        if (previousRow >= 0 && previousColumn >= 0)
            adjacentCells.add(gameGrid[previousRow][previousColumn]);

        boolean hasAnyPendingMines = false;
        for (var adjacentCell : adjacentCells) {

            if (adjacentCell.isMined && !adjacentCell.isFlagged) {
                hasAnyPendingMines = true;
                break;
            }
        }

        if (!hasAnyPendingMines) {

            for (var adjacentCell : adjacentCells) {

                if (adjacentCell.isMined)
                    continue;

                adjacentCell.isOpen = true;
            }
        }
    }

    public void checkForCleanCells(Cell selectedCell, int selectedRow, int selectedColumn) {

        //I don't need to evaluate mine cells (9) and adjacent to mine cells, just the empty cells (0)
        if (selectedCell.cellValue > 0)
            return;

        var result = floodFill(gameGrid, selectedRow, selectedColumn);

        for (int row = 0; row < totalRows; row++) {

            for (int column = 0; column < totalColumns; column++) {

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

                        if (nextColumn < totalColumns && gameGrid[row][nextColumn].index == adjacentIndex)
                            gameGrid[row][nextColumn].isOpen = true;

                        if (previousColumn >= 0 && gameGrid[row][previousColumn].index == adjacentIndex)
                            gameGrid[row][previousColumn].isOpen = true;

                        if (previousRow >= 0 && gameGrid[previousRow][column].index == adjacentIndex)
                            gameGrid[previousRow][column].isOpen = true;

                        if (nextRow < totalRows && nextColumn < totalColumns && gameGrid[nextRow][nextColumn].index == adjacentIndex)
                            gameGrid[nextRow][nextColumn].isOpen = true;

                        if (nextRow < totalRows && previousColumn >= 0 && gameGrid[nextRow][previousColumn].index == adjacentIndex)
                            gameGrid[nextRow][previousColumn].isOpen = true;

                        if (previousRow >= 0 && nextColumn < totalColumns && gameGrid[previousRow][nextColumn].index == adjacentIndex)
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

    public void dispose () {

        for (var tileNumberTexture : tileNumberTextures)
            tileNumberTexture.dispose();

        tileNumberTextures.clear();
    }
}

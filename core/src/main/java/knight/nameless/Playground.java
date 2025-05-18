package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private int[][] gameGrid;
    private Array<Integer> selectedCellsIndex;
    float circleX = 200;
    float circleY = 100;

    @Override
    public void create() {

        camera = new OrthographicCamera();
        camera.position.set(screenWidth / 2f, screenHeight / 2f, 0);

        viewport = new ExtendViewport(screenWidth, screenHeight, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        gameGrid = new int[TOTAL_ROWS][TOTAL_COLUMNS];

        initializeGrid(gameGrid);

        selectedCellsIndex = new Array<>();
    }

    private void initializeGrid(int[][] grid) {

        int index = 0;

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                grid[row][column] = index;
                index++;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void drawGrid(ShapeRenderer shapeRenderer) {

        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                Rectangle actualCell = new Rectangle(
                    column * CELL_SIZE + HORIZONTAL_OFFSET,
                    row * CELL_SIZE + VERTICAL_OFFSET,
                    CELL_SIZE - CELL_OFFSET,
                    CELL_SIZE - CELL_OFFSET
                );

                if (Gdx.input.justTouched() && mouseBounds.overlaps(actualCell)) {

                    int selectedIndex = gameGrid[row][column];
                    selectedCellsIndex.add(selectedIndex);
                }

                shapeRenderer.setColor(0.74f, 0.74f, 0.74f, 1);
                shapeRenderer.rect(actualCell.x, actualCell.y, actualCell.width, actualCell.height);
            }
        }
    }


    @Override
    public void render() {

        ScreenUtils.clear(Color.BLACK);

//        batch.setProjectionMatrix(viewport.getCamera().combined);
//        batch.begin();
//
//        batch.end();


        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        circleX = worldCoordinates.x;
        circleY = worldCoordinates.y;


        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

//        shapeRenderer.setColor(Color.LIGHT_GRAY);
//        shapeRenderer.rect(0, 0, 200, 200);

        drawGrid(shapeRenderer);

        for (int row = 0; row < TOTAL_ROWS; row++) {

            for (int column = 0; column < TOTAL_COLUMNS; column++) {

                Rectangle actualCell = new Rectangle(
                    column * CELL_SIZE + HORIZONTAL_OFFSET,
                    row * CELL_SIZE + VERTICAL_OFFSET,
                    CELL_SIZE - CELL_OFFSET,
                    CELL_SIZE - CELL_OFFSET
                );

                var actualIndex = gameGrid[row][column];

                for (var actual : selectedCellsIndex) {

                    if (actual == actualIndex) {

                        shapeRenderer.setColor(Color.DARK_GRAY);
                        shapeRenderer.rect(actualCell.x, actualCell.y, actualCell.width, actualCell.height);
                    }
                }
            }
        }

//        shapeRenderer.setColor(0, 0, 1, 1);
//        shapeRenderer.circle(circleX, circleY, 10);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}

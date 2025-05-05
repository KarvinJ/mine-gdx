package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Playground extends ApplicationAdapter {
    public final int screenWidth = 640;
    public final int screenHeight = 480;
    public ExtendViewport viewport;
    public OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private Texture image;
    float circleX = 200;
    float circleY = 100;

    @Override
    public void create() {

        camera = new OrthographicCamera();
        camera.position.set(screenWidth / 2f, screenHeight / 2f, 0);

        //ExtendViewport works well in almost any configuration and FitViewPort is better when the
        // game resolution or your game is lower than the device.
        viewport = new ExtendViewport(screenWidth , screenHeight, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {

        if (Gdx.input.isTouched()) {

            Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            circleX = worldCoordinates.x;
            circleY = worldCoordinates.y;

            //simple way to get the correct y position, this will fail when using viewports,
            // so now I know that camera unProject is the definitive way to get the mouse position.
//  returns the Y value of the touch or click. Note that because OpenGL renders with Y starting at
//  the bottom instead of the top, you can map from touch coordinates to render coordinates by
//  subtracting from the height:
//            circleY = Gdx.graphics.getHeight() - Gdx.input.getY();
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        batch.draw(image, 140, 210);

        batch.end();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.circle(circleX, circleY, 30);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}

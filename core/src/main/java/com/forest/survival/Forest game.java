package com.forest.survival;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ForestGame extends ApplicationAdapter {

    private PerspectiveCamera camera;
    private ModelBatch batch;

    private Model groundModel;
    private Model treeModel;
    private Model canopyModel;
    private Model rockModel;

    private final Array<ModelInstance> objects = new Array<>();

    private float yaw = 0f;
    private float pitch = 0f;

    private float lastX;
    private float lastY;

    private boolean dragging = false;

    @Override
    public void create() {
        batch = new ModelBatch();

        camera = new PerspectiveCamera(
                70f,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        camera.position.set(0f, 1.7f, 6f);
        camera.direction.set(0f, 0f, -1f);
        camera.near = 0.1f;
        camera.far = 120f;
        camera.update();

        ModelBuilder builder = new ModelBuilder();

        long attributes =
                VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal;

        groundModel = builder.createBox(
                100f,
                0.2f,
                100f,
                material(new Color(0.20f, 0.32f, 0.16f)),
                attributes
        );

        treeModel = builder.createCylinder(
                0.6f,
                4f,
                0.6f,
                8,
                material(new Color(0.30f, 0.18f, 0.09f)),
                attributes
        );

        canopyModel = builder.createCone(
                4f,
                6f,
                4f,
                8,
                material(new Color(0.10f, 0.30f, 0.12f)),
                attributes
        );

        rockModel = builder.createSphere(
                1.5f,
                1f,
                1.2f,
                12,
                8,
                material(new Color(0.40f, 0.41f, 0.39f)),
                attributes
        );

        // Forest floor
        addObject(groundModel, 0f, -0.1f, 0f);

        // Generate trees and rocks
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {

                // Keep the starting area clear
                if (Math.abs(x) < 2 && Math.abs(z) < 2) {
                    continue;
                }

                float px = x * 5f;
                float pz = z * 5f;

                if ((x + z) % 3 == 0) {
                    addObject(treeModel, px, 2f, pz);
                    addObject(canopyModel, px, 6f, pz);
                }

                if ((x * 7 + z * 3) % 11 == 0) {
                    addObject(rockModel, px + 1f, 0.4f, pz);
                }
            }
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private Material material(Color color) {
        return new Material(
                ColorAttribute.createDiffuse(color)
        );
    }

    private void addObject(
            Model model,
            float x,
            float y,
            float z
    ) {
        ModelInstance instance = new ModelInstance(model);

        instance.transform.setToTranslation(x, y, z);

        objects.add(instance);
    }

    @Override
    public void render() {
        float delta = Math.min(
                Gdx.graphics.getDeltaTime(),
                0.05f
        );

        updatePlayer(delta);

        Gdx.gl.glClearColor(
                0.55f,
                0.72f,
                0.85f,
                1f
        );

        Gdx.gl.glClear(
                GL20.GL_COLOR_BUFFER_BIT |
                GL20.GL_DEPTH_BUFFER_BIT
        );

        camera.update();

        batch.begin(camera);

        for (ModelInstance object : objects) {
            batch.render(object);
        }

        batch.end();
    }

    private void updatePlayer(float delta) {

        Vector3 forward = new Vector3(
                camera.direction.x,
                0f,
                camera.direction.z
        ).nor();

        Vector3 right = new Vector3(
                forward.z,
                0f,
                -forward.x
        );

        Vector3 movement = new Vector3();

        // Keyboard movement for testing
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movement.add(forward);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movement.sub(forward);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement.sub(right);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement.add(right);
        }

        if (movement.len2() > 0f) {
            movement.nor().scl(5f * delta);
            camera.position.add(movement);
        }

        // Touch-drag camera rotation
        if (Gdx.input.isTouched()) {

            float x = Gdx.input.getX();
            float y = Gdx.input.getY();

            if (dragging) {

                yaw -= (x - lastX) * 0.25f;

                pitch = Math.max(
                        -80f,
                        Math.min(
                                80f,
                                pitch + (lastY - y) * 0.25f
                        )
                );

                camera.direction
                        .set(0f, 0f, -1f)
                        .rotate(Vector3.Y, yaw)
                        .rotate(Vector3.X, pitch)
                        .nor();
            }

            lastX = x;
            lastY = y;

            dragging = true;

        } else {
            dragging = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null && width > 0 && height > 0) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    @Override
    public void dispose() {

        if (batch != null) {
            batch.dispose();
        }

        if (groundModel != null) {
            groundModel.dispose();
        }

        if (treeModel != null) {
            treeModel.dispose();
        }

        if (canopyModel != null) {
            canopyModel.dispose();
        }

        if (rockModel != null) {
            rockModel.dispose();
        }

        objects.clear();
    }
}

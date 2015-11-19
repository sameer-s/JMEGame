package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.DirectionalLight;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import mygame.ThirdPersonCharacter.Animations;
import mygame.ThirdPersonCharacter.Movement;
import mygame.ThirdPersonCharacter.SpatialProperties;
import mygame.ThirdPersonCharacter.ThirdPersonCamera.CameraProperties;

public class Main extends SimpleApplication
{
    private ThirdPersonCharacter player;

    public static void main(String... args)
    {
        Main gp = new Main();
        gp.start();
    }

    @Override
    public void simpleInitApp()
    {
        // Disables the default diagnostic
        setDisplayFps(false);
        setDisplayStatView(false);

        mouseInput.setCursorVisible(false);
        flyCam.setEnabled(false);

        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        Spatial sceneModel = assetManager
                .loadModel("Scenes/ManyLights/Main.scene");
        sceneModel.scale(1f, .5f, 1f);
        CollisionShape sceneShape = CollisionShapeFactory
                .createMeshShape(sceneModel);
        RigidBodyControl scene = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(scene);
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(scene);

        Spatial playerModel = assetManager
                .loadModel("Models/MainCharacter/MainCharacter_exp.j3o");
        playerModel.scale(1.2f);
        // playerModel.setLocalTranslation(0f, 0f, 0f);
        playerModel.rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);

        player = new ThirdPersonCharacter(
                playerModel,
                inputManager,
                cam,
                new Animations("Armature.001Action", "Walk"),
                Movement.DEFAULT,
                SpatialProperties.DEFAULT,
                CameraProperties.DEFAULT);

        player.getControl().warp(new Vector3f(-5f, 2f, 5f));
        rootNode.attachChild(player);
        bulletAppState.getPhysicsSpace().add(player);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-.1f, -.7f, -1f));
        rootNode.addLight(sun);
    }

    @Override
    public void simpleUpdate(float tpf)
    {
        player.update();
    }

    @SuppressWarnings("unused")
    private void log(Object tag, Object val)
    {
        System.out.println(tag.toString() + ": " + val.toString());
    }
}

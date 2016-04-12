package mygame.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import java.util.Random;
import java.util.UUID;
import mygame.debug.DebugLogger;
import mygame.scene.DestructibleGhost;
import mygame.scene.character.RotationLockedChaseCamera;
import org.lwjgl.input.Keyboard;

/**
 * The main class for the client application. It has a number of purposes:
 * This is the starting point for the game.
 * Handles the app states that take care of the game
 * Handles the player controller and other player spatial for the characters.
 * *
 * @author Sameer Suri
 */
public class Main extends SimpleApplication implements ActionListener
{
    /* Game stuff: */

    public static Main instance;

    private String name;

    private RotationLockedChaseCamera player1Cam, player2Cam;

    private Camera cam2;

    private static final String SHIP_MODEL = "Models/ship/SpaceShip.j3o";

    public static void main(String... args)
    {
        Main gp = new Main();
        instance = gp;
        gp.start();
    }

    public Main()
    {
        super(new AppState[] {});
    }

    // Called by the engine in order to start the game.
    @Override
    public void simpleInitApp()
    {
        this.getSettings().setWidth(this.getSettings().getWidth() / 2);
        settings.setUseJoysticks(true);

        this.setPauseOnLostFocus(false);

        inputManager.setCursorVisible(false);

        rootNode.setName("RootNode");

        name = UUID.randomUUID().toString().split("-")[0];

        Node player1Node = new Node();
        Spatial player1Model = assetManager.loadModel(SHIP_MODEL);
        player1Model.setLocalTranslation(0, -2.5f, 0);
        player1Model.setName("Player1" + name);
        player1Node.attachChild(player1Model);
        player1Node.setLocalTranslation(0, -2.5f, 0);
        rootNode.attachChild(player1Node);
        player1Cam = new RotationLockedChaseCamera(cam, player1Node, inputManager);

        cam2 = cam.clone();
        Node player2Node = new Node();
        Spatial player2Model = assetManager.loadModel(SHIP_MODEL);
        player2Model.setLocalTranslation(0, -2.5f, 0);
        player2Model.setName("Player2" + name);
        player2Node.attachChild(player2Model);
        player2Node.setLocalTranslation(0, 2.5f, 0);
        rootNode.attachChild(player2Node);
        player2Cam = new RotationLockedChaseCamera(cam2, player2Node, inputManager);

        for(int i = 0; i < player2Cam.movementActions.length; i++)
        {
            player2Cam.movementActions[i] = player2Cam.movementActions[i] + "_Player2";
        }
        inputManager.addListener(player2Cam, player2Cam.movementActions);

        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);

        for(int i = 0; i < 25; i++)
        {
            Geometry geom = new Geometry("RedBox", new Box(1f, 1f, 1f));
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            boxMat.setColor("Color", ColorRGBA.Red);
            geom.setLocalTranslation(i, i % 2 == 0 ? 25 - i : i, new Random().nextInt(25));
            geom.setMaterial(boxMat);
            geom.addControl(new DestructibleGhost(new BoxCollisionShape(new Vector3f(1f, 1f, 1f)), this, true));

            rootNode.attachChild(geom);
            stateManager.getState(BulletAppState.class).getPhysicsSpace().add(geom);
        }

        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Starfield.dds", EnvMapType.CubeMap));

        inputManager.addMapping("MouseFocus", new KeyTrigger(Keyboard.KEY_X));
        inputManager.addMapping("Debug", new KeyTrigger(Keyboard.KEY_B));
        inputManager.addMapping("ReloadJoysticks", new KeyTrigger(Keyboard.KEY_R));

        inputManager.addListener(this, "MouseFocus", "Debug", "ReloadJoysticks");

        JoystickInit.init(inputManager);

        // handling split screen
        cam.setViewPort(0f, .5f, 0f, 1f);
        cam2.setViewPort(.5f, 1f, 0f, 1f);

        viewPort = renderManager.createMainView("Player 1 View", cam);
        viewPort.setClearFlags(true, true, true);
        viewPort.attachScene(rootNode);
        viewPort.setBackgroundColor(ColorRGBA.Red);

        ViewPort viewPort2 = renderManager.createMainView("Player 2 View", cam2);
        viewPort2.setClearFlags(true, true, true);
        viewPort2.attachScene(rootNode);
        viewPort2.setBackgroundColor(ColorRGBA.Black);

        cam.setFrustumPerspective(45f, (float)cam.getWidth() / (2 * cam.getHeight()), 1f, 1000f);
        cam2.setFrustumPerspective(45f, (float)cam.getWidth() / (2 * cam.getHeight()), 1f, 1000f);

        cam.update();
        cam2.update();
    }

    @Override
    public void simpleUpdate(float tpf)
    {
    }

    // Cleans up resources, closes the window, and kills the app.
    @Override
    public void destroy()
    {
        DebugLogger.close();

        // Has the superclass finish cleanup
        super.destroy();

        // Kills the JVM
        System.exit(0);
    }

    // Called by an app state when it is finished.

    /**
     * Gets the settings established by the player in the settings menu.
     * @return The settings.
     */
    public AppSettings getSettings()
    {
        return settings;
    }

    public void addSpatial(Spatial sp, PhysicsControl... pcs)
    {
        rootNode.attachChild(sp);
        for(PhysicsControl pc : pcs)
        {
            sp.addControl(pc);
            this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(pc);
        }
    }

    public void addPhysicsTickListener(PhysicsTickListener... tickListeners)
    {
        for(PhysicsTickListener tickListener : tickListeners)
        {
           this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addTickListener(tickListener);
        }
    }

    public void removeSpatial(final Spatial spatial)
    {
        enqueue(() -> {
            try
            {
                spatial.removeFromParent();
                getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(spatial);

                while(spatial.getNumControls() > 0)
                {
                    spatial.removeControl(spatial.getControl(0));
                }

                nullifySpatial(spatial);
            }
            catch(NullPointerException npe) {}

            return 0;
        });
    }

    private void nullifySpatial(Spatial spatial)
    {
        spatial = null;
    }


    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        switch(name)
        {
            case "MouseFocus":
                if(isPressed)
                {
                    player1Cam.setDragToRotate(!player1Cam.isDragToRotate());
                    player2Cam.setDragToRotate(!player2Cam.isDragToRotate());
                }
                break;
            case "Debug":
                if(isPressed)
                {
                    this.getStateManager().getState(BulletAppState.class).setDebugEnabled(!this.getStateManager().getState(BulletAppState.class).isDebugEnabled());

                    StatsAppState stats = this.getStateManager().getState(StatsAppState.class);
                    if(stats == null) this.getStateManager().attach(new StatsAppState());
                    else this.getStateManager().detach(stats);
                }
                break;
            case "ReloadJoysticks":
                if(isPressed)
                {
                    JoystickInit.init(inputManager);
                }
                break;
        }
    }
}

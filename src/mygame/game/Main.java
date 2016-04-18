package mygame.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.font.BitmapText;
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
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Random;
import mygame.debug.DebugLogger;
import mygame.scene.DestructibleCollisionListener;
import mygame.scene.DestructibleGhost;
import mygame.scene.character.RotationLockedChaseCamera;
import mygame.scene.character.ShipCharacterControl;
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

    private RotationLockedChaseCamera player1Cam, player2Cam;

    private Camera cam2;

    private static final String SHIP_MODEL = "Models/ship/SpaceShip.j3o";

    private HashMap<String, Spatial> guiElements = new HashMap<>();

    private String loser = null;

    public static void main(String... args)
    {
        AppSettings settings = new AppSettings(true);
        settings.setUseJoysticks(true);
        settings.setFullscreen(true);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        settings.setWidth(gd.getDisplayMode().getWidth());
        settings.setHeight(gd.getDisplayMode().getHeight());

        Main gp = new Main();
        instance = gp;
        gp.setSettings(settings);
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
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
        bulletAppState.getPhysicsSpace().addCollisionListener(new DestructibleCollisionListener());

        this.setPauseOnLostFocus(false);

        inputManager.setCursorVisible(false);

        Node player1Node = new Node();
        Spatial player1Model = assetManager.loadModel(SHIP_MODEL);
        player1Model.setLocalTranslation(0, -2.5f, 0);
        player1Model.setName("Player1");
        player1Node.attachChild(player1Model);
        player1Node.setLocalTranslation(0, 0, 50);
        player1Node.addControl(new ShipCharacterControl(player1Node, "").initKeys(inputManager).registerMappings(inputManager));
        rootNode.attachChild(player1Node);
        stateManager.getState(BulletAppState.class).getPhysicsSpace().add(player1Node);
        player1Cam = new RotationLockedChaseCamera(cam, player1Node, inputManager);


        cam2 = cam.clone();
        Node player2Node = new Node();
        Spatial player2Model = assetManager.loadModel(SHIP_MODEL);
        player2Model.setLocalTranslation(0, -2.5f, 0);
        player2Model.setName("Player2");
        player2Node.attachChild(player2Model);
        player2Node.setLocalTranslation(50, 0, 0);
        player2Node.addControl(new ShipCharacterControl(player2Node, "_Player2").registerMappings(inputManager));
        rootNode.attachChild(player2Node);
        stateManager.getState(BulletAppState.class).getPhysicsSpace().add(player2Node);
        player2Cam = new RotationLockedChaseCamera(cam2, player2Node, inputManager);


        for(int i = 0; i < player2Cam.movementActions.length; i++)
        {
            player2Cam.movementActions[i] = player2Cam.movementActions[i] + "_Player2";
        }
        inputManager.addListener(player2Cam, player2Cam.movementActions);

        for(int i = 0; i < 25; i++)
        {
            Geometry geom = new Geometry("BlueBox" + i, new Box(1f, 1f, 1f));
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            boxMat.setColor("Color", ColorRGBA.Blue);
            geom.setLocalTranslation(i, i % 2 == 0 ? 25 - i : i, new Random().nextInt(25));
            geom.setMaterial(boxMat);
            geom.addControl(new DestructibleGhost(new BoxCollisionShape(new Vector3f(1f, 1f, 1f)), true));

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

        ViewPort viewPort2 = renderManager.createMainView("Player 2 View", cam2);
        viewPort2.setClearFlags(true, true, true);
        viewPort2.attachScene(rootNode);

        System.out.printf("width=%d, height=%d%n", settings.getWidth(), settings.getHeight());
        System.out.println((float) settings.getWidth() / (2 * settings.getHeight()));

        cam.setFrustumPerspective(45f, (float) settings.getWidth() / (2 * settings.getHeight()), 1f, 1000f);
        cam2.setFrustumPerspective(45f, (float) settings.getWidth() / (2 * settings.getHeight()), 1f, 1000f);

        cam.update();
        cam2.update();

        initGui();
    }

    private void initGui()
    {
        BitmapText scoreText1 = new BitmapText(guiFont, false);
        scoreText1.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText1.setColor(ColorRGBA.White);
        scoreText1.setText("Score: 0");
        scoreText1.setLocalTranslation(10, settings.getHeight(), 0);
        guiNode.attachChild(scoreText1);
        guiElements.put("P1Score", scoreText1);

        BitmapText scoreText2 = new BitmapText(guiFont, false);
        scoreText2.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText2.setColor(ColorRGBA.White);
        scoreText2.setText("Score: 0");
        scoreText2.setLocalTranslation(settings.getWidth() - scoreText2.getLineWidth() - 10,
                settings.getHeight(), 0);
        guiNode.attachChild(scoreText2);
        guiElements.put("P2Score", scoreText2);
    }

    private BitmapText getText(String name)
    {
        return (BitmapText) guiElements.get(name);
    }

    private Picture getPicture(String name)
    {
        return (Picture) guiElements.get(name);
    }

    @Override
    public void simpleUpdate(float tpf)
    {
        int winner = loser == null ? 0 : (loser.equals("") ? 2 : 1);
        getText("P1Score").setText("Score: " + winner % 2);
        getText("P2Score").setText("Score: " + winner / 2);
        getText("P2Score").setLocalTranslation(
                settings.getWidth() - getText("P2Score").getLineWidth() - 10,
                settings.getHeight(), 0);
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
            }
            catch(NullPointerException npe) {}

            return 0;
        });
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
                    System.out.println("Reloading Joysticks");
                    JoystickInit.init(inputManager);
                }
                break;
        }
    }

    public void playerLoses(String code)
    {
        this.loser = code;
    }
}

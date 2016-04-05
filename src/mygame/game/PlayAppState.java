package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import java.util.Random;
import java.util.function.Consumer;
import mygame.debug.DebugLogger;
import mygame.network.message.PlayerInformationMessage;
import mygame.scene.DestructibleGhost;
import mygame.scene.character.NetworkedCharacterControl;
import mygame.scene.character.RotationLockedChaseCamera;
import mygame.scene.character.ThirdPersonCharacterControl;
import org.lwjgl.input.Keyboard;

/**
 * The app state where the player is interacting with the world and other player.
 * This is the main game, and all of the actual game code is here.
 * @author Sameer Suri
 */
public class PlayAppState extends AbstractAppState implements ActionListener
{
    private static final String SHIP_MODEL = "Models/ship/SpaceShip.j3o";

    ChaseCamera chaseCam;

    private StatsAppState stats;

    // When the app state first starts
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        // Has the super class take care of some stuff
        super.initialize(stateManager, app);

        Main.instance.getViewPort().setBackgroundColor(ColorRGBA.Black);

        stats = new StatsAppState();

        // Sets up the Bullet Physics Engine
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);

        // Loads the space scene
        Main.instance.getRootNode().attachChild(SkyFactory.createSky(
                Main.instance.getAssetManager(), "Textures/Starfield.dds", EnvMapType.CubeMap));
        // Loads the player model
        Spatial playerModel = Main.instance.getAssetManager().loadModel(SHIP_MODEL);
        Spatial opponentModel = Main.instance.getAssetManager().loadModel(SHIP_MODEL);

        Recursive<Consumer<Spatial>> enableVertexColors = new Recursive<>();

        enableVertexColors.function = sp ->
        {
            if(sp instanceof Node)
            {
                ((Node) sp).getChildren().stream().forEach(enableVertexColors.function);
            }
            else if(sp instanceof Geometry)
            {
                Material geomMat = ((Geometry) sp).getMaterial();

                try
                {
                    geomMat.setBoolean("VertexColor", true);
                }
                catch(IllegalArgumentException e)
                {
                    DebugLogger.println(e.getLocalizedMessage());
                }

                ((Geometry) sp).setMaterial(geomMat);
            }
        };

        enableVertexColors.function.accept(playerModel);
        enableVertexColors.function.accept(opponentModel);

        // Makes some adjustment so it works properly
        playerModel.scale(.5f);
        opponentModel.scale(.5f);

        // Creates our new character controller, passing in a few necessary parameters.
        Main.instance.playerController = new ThirdPersonCharacterControl(playerModel, Main.instance.getCamera());
        Main.instance.playerController.initKeys(Main.instance.getInputManager());

        Main.instance.otherPlayerController = new NetworkedCharacterControl();

        // Attaches the control to the player model
        playerModel.addControl(Main.instance.playerController);
        opponentModel.addControl(Main.instance.otherPlayerController);

        // Attaches the model to the root node
        // This makes it appear in the world
        Main.instance.getRootNode().attachChild(playerModel);
        Main.instance.getRootNode().attachChild(opponentModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(playerModel);
        bulletAppState.getPhysicsSpace().add(opponentModel);


        chaseCam = new RotationLockedChaseCamera(Main.instance.getCamera(), playerModel, Main.instance.getInputManager());
        // By default, you have to push down a mouse button to rotate the chase cam. This disables that.
        chaseCam.setDragToRotate(false);
        // By default, it looks at the player model's (0,0,0), which is at its feet. This looks a bit higher.
        chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0));
        // This keeps the camera a bit closer to the player than the default. This can be changed by the scroll wheel (on the mouse).
        chaseCam.setDefaultDistance(7f);
        // Speeds up the rotation, as the default is quite slow.
        chaseCam.setRotationSpeed(2f);

        chaseCam.setMinVerticalRotation(-(7 * FastMath.PI) / 16);

        // Creates a sun (a light) so that the player can see.
        DirectionalLight sun = new DirectionalLight();
        // Gives it an angle to add more realism
        sun.setDirection(new Vector3f(-.1f, -.7f, -1f));
        // Places it in the world
        Main.instance.getRootNode().addLight(sun);

        // Informs the client message listener of the current app states
        Main.instance.clientMessageListener.setAppState(this);

        // Has this listen to certain keypresses during the game.
        // These are initialized in initKeys of ThirdPersonCharacterControl
        // The rest of them are also handled there
        Main.instance.getInputManager().addMapping("Debug", new KeyTrigger(Keyboard.KEY_B));
        Main.instance.getInputManager().addMapping("MouseCapture", new KeyTrigger(Keyboard.KEY_X));
        Main.instance.getInputManager().addListener(this, "Debug", "MouseCapture");

        for(int i = 0; i < 25; i++)
        {
            Geometry geom = new Geometry("RedBox", new Box(1f, 1f, 1f));
            Material boxMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            boxMat.setColor("Color", ColorRGBA.Red);
            geom.setLocalTranslation(i, i % 2 == 0 ? 25 - i : i, new Random().nextInt(25));
            geom.setMaterial(boxMat);
            geom.addControl(new DestructibleGhost(new BoxCollisionShape(new Vector3f(1f, 1f, 1f)), Main.instance, true));

            Main.instance.getRootNode().attachChild(geom);
            Main.instance.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(geom);
        }
    }

    // Called in a loop by the engine to allow the game to make changes.
    @Override
    public void update(float tpf)
    {
        // Updates the player controller, allowing it to move the player and handle animation changes.
        Main.instance.playerController.update(tpf);

        if(Main.instance.client.isStarted())
        {
            // Sends out the current state of OUR player to the other client, in message form
            Main.instance.client.send(Main.instance.playerController.toMessage());
        }
//        app.otherPlayerController.update(tpf);
    }

    // Called by our message listener when the other player sends out its info
    public void updateOpponentLocation(PlayerInformationMessage m)
    {
        Main.instance.otherPlayerController.recieveMessage(m);
    }

    // Called by our message listener when the other player disconnects
    public void opponentDisconnected()
    {
        /*
        // Stops the app
        Main.instance.stop();
        // Gives the player a message. Eventually, this will probably instead go to a main menu.
        JOptionPane.showMessageDialog(
            null,
            "Looks like your opponent disconnected. Thanks for playing!",
            "Opponent disconnected",
            JOptionPane.ERROR_MESSAGE);
        // Exits the JVM
        System.exit(0);
        */
    }

    // Called when a button press or other registered action occurs
    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        // If it is a debug action, and we are pressing the button
        if(name.equals("Debug") && isPressed)
        {
            // Enable debug mode for the physics engine (show colliders)
            Main.instance.getStateManager().getState(BulletAppState.class).setDebugEnabled(!Main.instance.getStateManager().getState(BulletAppState.class).isDebugEnabled());
            if(Main.instance.getStateManager().hasState(stats))
            {
                Main.instance.getStateManager().detach(stats);
            }
            else
            {
                Main.instance.getStateManager().attach(stats);
            }
        }
        else if(name.equals("MouseCapture") && isPressed)
        {
            if(chaseCam != null)
            {
                chaseCam.setDragToRotate(!chaseCam.isDragToRotate());
            }
        }
    }

    private static class Recursive<T>
    {
        public T function;
    }
}

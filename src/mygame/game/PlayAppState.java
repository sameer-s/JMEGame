package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.ActionListener;
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
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import mygame.character.RotationLockedChaseCamera;
import mygame.character.ThirdPersonCharacterControl;
import mygame.network.message.PlayerInformationMessage;

/**
 * The app state where the player is interacting with the world and other player.
 * This is the main game, and all of the actual game code is here.
 * @author Sameer Suri
 */
public class PlayAppState extends AbstractAppState implements ActionListener
{
    // Holds a reference to the application
    private Main app;

    private static final String SHIP_MODEL = "Models/ship/v2.j3o";

    // When the app state first starts
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        // Has the super class take care of some stuff
        super.initialize(stateManager, app);

        // Casts the app to our Main type and stores it for later use
        this.app = (Main) app;

        this.app.getViewPort().setBackgroundColor(ColorRGBA.Black);

        // Sets up the Bullet Physics Engine
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);

        Geometry[] cubes = new Geometry[] {
//          new Geometry("cube1"),
//          new Geometry("cube2"),
//          new Geometry("cube3"),
        };

        Material mat = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        Stream.of(cubes).forEach(cube -> {
            int i = Arrays.asList(cubes).indexOf(cube);

            Box b = new Box(1, 1, 1);
            cube.setMesh(b);

            if(i == 0)
            {
                Material mat0 = mat.clone();

                mat0.setColor("Color", ColorRGBA.Red);

                cube.setMaterial(mat0);
            }
            else
            {
                mat.setColor("Color", ColorRGBA.Blue);
                cube.setMaterial(mat);
            }

            this.app.getRootNode().attachChild(cube);

            cube.setLocalTranslation(0, 0, 3*i);
        });

        // Loads the space scene
        this.app.getRootNode().attachChild(SkyFactory.createSky(
                this.app.getAssetManager(), "Textures/Starfield.dds", EnvMapType.CubeMap));
        // Loads the player model
        Spatial playerModel = this.app.getAssetManager().loadModel(SHIP_MODEL);

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
                geomMat.setBoolean("UseVertexColor", true);
                ((Geometry) sp).setMaterial(geomMat);
            }
        };

        // Makes some adjustment so it works properly
        playerModel.scale(.5f);

        // Creates our new character controller, passing in a few necessary parameters.
        this.app.playerController = new ThirdPersonCharacterControl(playerModel, this.app.getCamera(), this.app);
        this.app.playerController.initKeys(this.app.getInputManager());

        // Attaches the control to the player model
        playerModel.addControl(this.app.playerController);

        // Attaches the model to the root node
        // This makes it appear in the world
        this.app.getRootNode().attachChild(playerModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(playerModel);


        ChaseCamera chaseCam = new RotationLockedChaseCamera(this.app.getCamera(), playerModel, this.app.getInputManager());
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
        this.app.getRootNode().addLight(sun);

        // Informs the client message listener of the current app states
        this.app.clientMessageListener.setAppState(this);

        // Has this listen to certain keypresses during the game.
        // These are initialized in initKeys of ThirdPersonCharacterControl
        // The rest of them are also handled there
        this.app.getInputManager().addListener(this, "Debug");
        
        Geometry geom = new Geometry("RedBox", new Box(1, 1, 1));
        Material boxMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Red);
        geom.setLocalTranslation(5, 5, 5);
        geom.setMaterial(boxMat);
        geom.addControl(new RigidBodyControl(1f));
        
        this.app.getRootNode().attachChild(geom);
    }


    // Called in a loop by the engine to allow the game to make changes.
    @Override
    public void update(float tpf)
    {
        // Updates the player controller, allowing it to move the player and handle animation changes.
        app.playerController.update(tpf);

        // Sends out the current state of OUR player to the other client, in message form
        app.client.send(app.playerController.toMessage());

//        app.otherPlayerController.update(tpf);
    }

    // Called by our message listener when the other player sends out its info
    public void updateOpponentLocation(PlayerInformationMessage m)
    {
        app.otherPlayerController.recieveMessage(m);
    }

    // Called by our message listener when the other player disconnects
    public void opponentDisconnected()
    {
        // Stops the app
        app.stop();
        // Gives the player a message. Eventually, this will probably instead go to a main menu.
        JOptionPane.showMessageDialog(
            null,
            "Looks like your opponent disconnected. Thanks for playing!",
            "Opponent disconnected",
            JOptionPane.ERROR_MESSAGE);
        // Exits the JVM
        System.exit(0);
    }

    // Called when a button press or other registered action occurs
    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        // If it is a debug action, and we are pressing the button
        if(name.equals("Debug") && isPressed)
        {
            // Enable debug mode for the physics engine (show colliders)
            app.getStateManager().getState(BulletAppState.class).setDebugEnabled(!app.getStateManager().getState(BulletAppState.class).isDebugEnabled());
        }
    }

    private static class Recursive<T>
    {
        public T function;
    }
}

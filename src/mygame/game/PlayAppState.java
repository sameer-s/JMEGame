package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import javax.swing.JOptionPane;
import mygame.character.NetworkedCharacterHandlers;
import mygame.character.ThirdPersonCharacterControl;
import static mygame.character.ThirdPersonCharacterControl._height;
import static mygame.character.ThirdPersonCharacterControl._mass;
import static mygame.character.ThirdPersonCharacterControl._radius;
import mygame.gui.GUIConsole;
import mygame.network.message.PlayerInformationMessage;
import org.lwjgl.input.Keyboard;

/**
 * The app state where the player is interacting with the world and other player.
 * This is the main game, and all of the actual game code is here.
 * @author Sameer Suri
 */
public class PlayAppState extends AbstractAppState implements ActionListener
{
    // Holds a reference to the application
    private Main app;

    // These are, for now, both the male model. This will be changed at some point.
    private static final String MALE_MODEL = "Models/MainCharacter3_2/MainCharacter3_2.j3o",
                              FEMALE_MODEL = "Models/MainCharacter3_2/MainCharacter3_2.j3o";

    // Maps the names that the Third Person Character Controller class uses to animations to that the model uses.
    // Holds the animations for the male and female player
    // Values set in setupAnimMaps()
    public static HashMap<String, String> MALE_ANIMS, FEMALE_ANIMS;

    private ChaseCamera chaseCam;

    /**
     * Sets up the maps with the names of the animations
     */
    private void setupAnimMaps()
    {
        // Male:
        MALE_ANIMS = new HashMap<>();
        MALE_ANIMS.put("Idle", "Idle");
        MALE_ANIMS.put("Move", "Running3");

        // Female: again, for now, the same. Will be changed.
        FEMALE_ANIMS = new HashMap<>();
        FEMALE_ANIMS.put("Idle", "Idle");
        FEMALE_ANIMS.put("Move", "Running3");
    }

    // When the app state first starts
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        // Has the super class take care of some stuff
        super.initialize(stateManager, app);

        // Casts the app to our Main type and stores it for later use
        this.app = (Main) app;

        // Sets up the Bullet Physics Engine
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // Loads the model used for the scene
        Spatial sceneModel = this.app.getAssetManager()
                .loadModel("Scenes/FirstScene.j3o");
//              .loadModel("Scenes/ManyLights/Main.scene");

        // Disables face culling, which means that both sides of the scene will be rendered, not just the top.
        // This makes the game appear less buggy when the camera goes below the world.
        disableFaceCulling(sceneModel);

        // Scales it to make it a little short
        sceneModel.scale(1f, 0.1f, 1f);
        // Generates a collider for physics collisions
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
        // Creates a control. What a control does is tell what it's controlling
        // what exactly to do. A Rigid Body Control is very simple: it's a rigid
        // body, meaning it does basically nothing. The rigid body is for the
        // scene, which needs not move.
        RigidBodyControl scene = new RigidBodyControl(sceneShape, 0);
        // Attaches the control to our model
        sceneModel.addControl(scene);
        // Attaches the model to the root node
        // This makes it appear in the world
        this.app.getRootNode().attachChild(sceneModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(scene);

        // Sets up the HashMaps we use for animation
        setupAnimMaps();
        

        // Loads the player model (player 1 -> male, player 2 -> female)
        Spatial playerModel = this.app.getAssetManager()
                .loadModel(this.app.isPlayer1 ? MALE_MODEL : FEMALE_MODEL);

        // Makes some adjustment so it works properly
        playerModel.scale(2f);
        playerModel.setLocalTranslation(20f, 20f, 20f);

        // Creates the chase camera. The chase camera is a camera which rotates
        // and zooms around the player. There are some configuration changes
        // I made, which I detail below:
        chaseCam = new ChaseCamera(this.app.getCamera(), playerModel, this.app.getInputManager());
        // By default, you have to push down a mouse button to rotate the chase cam. This disables that.
        chaseCam.setDragToRotate(false);
        // By default, it looks at the player model's (0,0,0), which is at its feet. This looks a bit higher.
        chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0));
        // This keeps the camera a bit closer to the player than the default. This can be changed by the scroll wheel (on the mouse).
        chaseCam.setDefaultDistance(7f);
        // Speeds up the rotation, as the default is quite slow.
        chaseCam.setRotationSpeed(2f);

        // Creates our new character controller, passing in a few necessary parameters.
        this.app.playerController = new ThirdPersonCharacterControl(
                this.app.isPlayer1 ? MALE_ANIMS : FEMALE_ANIMS,
                playerModel, this.app.getCamera());

        // Initializes button presses and joysticks
        this.app.playerController.initKeys(this.app.getInputManager(), this.app.isPlayer1 ? 1 : 0);

        // Attaches the control to the player model
        playerModel.addControl(this.app.playerController);

        // Attaches the model to the root node
        // This makes it appear in the world
        this.app.getRootNode().attachChild(playerModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(playerModel);

        // Loads the opposite model for your opponent
        this.app.otherPlayer = this.app.getAssetManager().loadModel(this.app.isPlayer1 ? FEMALE_MODEL : MALE_MODEL);
        // Makes some adjustment so it works properly
        this.app.otherPlayer.scale(2.f);
        this.app.otherPlayer.rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);
        this.app.otherPlayer.setLocalTranslation(0, -200f, 0f);

        // Attaches it to the root node
        this.app.getRootNode().attachChild(this.app.otherPlayer);
        bulletAppState.getPhysicsSpace().add(this.app.otherPlayer);

        // Sets up the animations for the other player
        NetworkedCharacterHandlers.AnimationHandler.init(this.app.otherPlayer, this.app.isPlayer1 ? FEMALE_ANIMS : MALE_ANIMS);
        // Gives it a 'rigid body' so it can exist in the Bullet Physics world
        RigidBodyControl otherPlayerControl = new RigidBodyControl(_mass);
        // Creates a collider based on a capsule.
        otherPlayerControl.setCollisionShape(new CapsuleCollisionShape(_radius, _height));
        // States that this rigid body moves.
        otherPlayerControl.setKinematic(true);
        // Adds the rigid body.
        this.app.otherPlayer.addControl(otherPlayerControl);

        // Creates a sun (a light) so that the player can see.
        DirectionalLight sun = new DirectionalLight();
        // Gives it an angle to add more realism
        sun.setDirection(new Vector3f(-.1f, -.7f, -1f));
        // Places it in the world
        this.app.getRootNode().addLight(sun);

        GUIConsole.CommandRunner commandRunner = (cmd) -> {
            // If nothing was inputted, do nothing!
            if(cmd == null) return;

            // Gets the first word in the command.
            switch(cmd.split(" ")[0])
            {
                // Currently does nothing
                default:
                    break;
            }
        };

        // Registers the GUI-based console
        // It will likely be temporary and only for debugging
        GUIConsole console = new GUIConsole();
        console.initKeys(this.app.getInputManager(), new KeyTrigger(Keyboard.KEY_T), commandRunner);

        // Informs the client message listener of the current app states
        this.app.clientMessageListener.setAppState(this);

        // Has this listen to certain keypresses during the game.
        // These are initialized in initKeys of ThirdPersonCharacterControl
        // The rest of them are also handled there
        this.app.getInputManager().addListener(this, "Disco", "Debug", "CaptureMouse");
    }

    // Variables to describe the current state of the other player
    private Vector3f otherCharacterLocation = new Vector3f();
    private Quaternion otherCharacterRotation = new Quaternion();
    private String[] otherCharacterAnims = null;

    // A flag. True if we are currently in disco mode, false otherwise.
    private boolean disco = false;
    // The time, for disco mode. Restarts (at zero) after every disco iteration.
    private float t = 0;
    // Called in a loop by the engine to allow the game to make changes.
    @Override
    public void update(float tpf)
    {
        // Updates the player controller, allowing it to move the player and handle animation changes.
        app.playerController.update(tpf);

        // Sends out the current state of OUR player to the other client, in message form
        app.client.send(app.playerController.toMessage());

        // Adjusts the other player's state based on what we have recieved
        app.otherPlayer.setLocalTranslation(NetworkedCharacterHandlers.MovementHandler.move(app.otherPlayer.getLocalTranslation(), otherCharacterLocation, tpf));
        app.otherPlayer.setLocalRotation(otherCharacterRotation);
        // avoid null pointer
        if(otherCharacterAnims != null)
        {
            // Update the animations on the opposing player based on what the messge listener recieved
            NetworkedCharacterHandlers.AnimationHandler.updateAnims(otherCharacterAnims);
        }


        // If time = 0 (we are ready to update the sky color)
        if(t == 0)
        {
            // Makes the sky blue if we are not in disco mode
            // Set it to a random color if we are
            this.app.getViewPort().setBackgroundColor(disco ? ColorRGBA.randomColor() : new ColorRGBA(0f, 127f/255f, 1f, 1f));
        }

        // Add the time taken this frame to the total time
        t += tpf;
        // Reset the timer every quarter second (this can be toggled for faster or slower disco)
        if(t >= .25) t = 0;
    }

    // Called by our message listener when the other player sends out its info
    public void updateOpponentLocation(PlayerInformationMessage m)
    {
        // Updates the corresponding variables
        otherCharacterLocation = new Vector3f(m.location[0], m.location[1], m.location[2]);
        otherCharacterRotation = new Quaternion(m.rotation[0], m.rotation[1], m.rotation[2], m.rotation[3]);
        otherCharacterAnims = m.currentAnims;
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
        // Log that an action occured. Debug statement.
        System.out.printf("Action, name=%s, isPressed=%b", name, isPressed);
        
        // If it is a disco action, and we are pressing the button (not releasing)
        if(name.equals("Disco") && isPressed)
        {
            // toggle disco mode
            disco = !disco;
            // reset the timer
            t = 0;
        }
        // If it is a debug action, and we are pressing the button
        else if(name.equals("Debug") && isPressed)
        {
            // Enable debug mode for the physics engine (show colliders)
            app.getStateManager().getState(BulletAppState.class).setDebugEnabled(!app.getStateManager().getState(BulletAppState.class).isDebugEnabled());
        }
        // The chase camera is the thing that captures the mouse.
        // By making it so that you have to press the mouse to rotate the camera, this uncaptures the mouse.
        // Hence, we toggle this setting.
        // Since it is confusing and useless to the user, this will be removed in release.
        else if(name.equals("CaptureMouse") && isPressed)
        {
            // Toggle whether you have to click to rotat
            chaseCam.setDragToRotate(!chaseCam.isDragToRotate());
        }
    }

    /**
     * Disables face culling for a provided spatial.
     * Should be used sparingly, as rendering both faces of a spatial takes more processing power.
     * @param spatial The spatial to disable face culling for.
     */
    private void disableFaceCulling(Spatial spatial)
    {
        // There are two types of spatials, geometries and nodes.
        // Geometries are actual objects, and nodes hold more spatials.
        // Since you can only disable culling for a geometry, for a node, you must [un]cull each child.

        // If it is a geometry
        if(spatial instanceof Geometry)
        {
            // Set its face culling mode to "Off" (cull no faces)
            ((Geometry) spatial).getMaterial().getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        }
        // Otherwise [it is a node]
        else if(spatial instanceof Node)
        {
            // Cull all of this children, by calling this method.
            // The recursion will allow it to go down the tree and cull all child geometries
            ((Node) spatial).getChildren().stream().forEach(child -> disableFaceCulling(child));
        }
    }
}

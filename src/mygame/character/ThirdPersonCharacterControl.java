package mygame.character;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import mygame.network.message.PlayerInformationMessage;
import org.lwjgl.input.Keyboard;

/**
 * A class to control the ship.
 * This class is built networkable and modular. It is NOT intended to be overridden.
 * To create your own networked game based off of this:
 * 1.) Copy over the NetworkedCharacterControl class
 * 2.) Copy over this class
 * 3.) Change this class to match your type of game (space, 3rd person, etc.)
 *
 * I will probably eventually release a library version of this that is MUCH easier to use.
 *
 * @author Sameer Suri
 */
public class ThirdPersonCharacterControl extends BetterCharacterControl
                                         implements ActionListener, AnalogListener
{

    // Constants describing the movement of the character
    private int throttle = 0;
    static final float maxSpeed = 3, rotationSensitivity = 100;

    // Constants describing the characteristics of the character's hitbox.
    static float _radius = .6f,_height = 3.4f, xOffset = 0, yOffset = .1f, zOffset = 0;

    static float _mass = 0f;

    // The instance of the JME camera class that we use to find out which way the player is looking.
    @SuppressWarnings("FieldMayBeFinal")
    protected Camera cam;

    /**
     * Constructor for the control.
     * @param spatial The model used, so that the control can find the animations in the model
     * @param cam The camera, so that the control can see where the player is looking
     */
    public ThirdPersonCharacterControl(Spatial spatial, Camera cam)
    {
        // Calls the BetterCharacterControl constructor, which, among other things
        // creates a Bullet Physics entity to the player, given a radius, height,
        // and mass (this would be represented as a capsule shape)
        super(_radius, _height, _mass);

        // Stores the camera in an instance variable
        this.cam = cam;
    }

    /**
     * Binds keys to their respective actions.
     * Also takes care of joystick input.
     * @param inputManager The input manager that is uesd for binding the actions.
     */
    public void initKeys(InputManager inputManager)
    {
        // Binds keys to their respective actions

//	inputManager.addMapping("RotL", new MouseAxisTrigger(MouseInput.AXIS_X, true));
//	inputManager.addMapping("RotR", new MouseAxisTrigger(MouseInput.AXIS_X, false));
//	inputManager.addMapping("RotU", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
//	inputManager.addMapping("RotD", new MouseAxisTrigger(MouseInput.AXIS_Y, false));


	inputManager.addMapping("RotL", new KeyTrigger(Keyboard.KEY_A));
	inputManager.addMapping("RotR", new KeyTrigger(Keyboard.KEY_D));

        inputManager.addMapping("Throttle+", new KeyTrigger(Keyboard.KEY_W));
        inputManager.addMapping("Throttle-", new KeyTrigger(Keyboard.KEY_S));

        // Adds listeners for the action
        // This allows 'this' object to be notified when one of the above set
        // keys is pressed
        inputManager.addListener(this, "RotL", "RotR", "RotD", "RotU", "Throttle+", "Throttle-");
    }

    private boolean rotL = false, rotR = false;
    // Notifies the character controller when a button event occurs.
    @Override
    public void onAction(String action, boolean isPressed, float tpf)
    {
        switch(action)
        {
            case "RotL":
                rotL = isPressed;
                break;
            case "RotR":
                rotR = isPressed;
                break;
            case "Throttle+":
                throttle = 100;
                break;
            case "Throttle-":
                throttle = 0;
                break;
        }
    }

    @Override
    public void onAnalog(String action, float value, float tpf)
    {
    }

    // Handles movement as the game goes on.
    @Override
    public void update(float tpf)
    {
        // Has the superclass take care of physics stuff
        super.update(tpf);

        Vector3f _rotation = this.cam.getRotation().mult(Vector3f.UNIT_XYZ);

//        System.out.printf("Cam rotation is [%f, %f, %f]%n", _rotation.x, _rotation.y, _rotation.z);
//        System.out.printf("View vector is [%f, %f, %f]%n", viewDirection.x, viewDirection.y, viewDirection.z);

        float[] deltaRotation = new float[]{0, 0, 0};
        if(rotL)
        {
            deltaRotation[2] += rotationSensitivity * tpf;
        }
        else if(rotR)
        {
            deltaRotation[2] += -rotationSensitivity * tpf;
        }

//        System.out.println(this.spatial.getLocalRotation().mult(Vector3f.UNIT_XYZ));
        this.setPhysicsRotation(this.getSpatialRotation().add(new Quaternion().fromAngles(deltaRotation)));
        this.spatial.rotate(new Quaternion().fromAngles(deltaRotation));

//        walkDirection
//        this.setPhysicsRotation(new Quaternion().fromAngles(1, 1, 1));

        System.out.printf("Delta Rotation: [%f, %f, %f]%nrotL=%b\trotR=%b%n", deltaRotation[0], deltaRotation[1], deltaRotation[2], rotL, rotR);
    }

    /**
     * Converts this object into a message to be networked.
     * @return The message.
     */
    public PlayerInformationMessage toMessage()
    {
        // Constructs the message
        PlayerInformationMessage message = new PlayerInformationMessage();

        // Gets the location and rotation data from this object and puts it in the messaeg
        message.location = new float[]
            {location.x, location.y, location.z};
        message.rotation = new float[]
            {rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW()};

        // Tells the message to use UDP rather than TCP protocol
        // TCP -> slow, reliable (no packet loss)
        // UDP -> fast, unreliable (packet loss, messages may appear in the wrong order)
        message.setReliable(false);
        // Returns the message
        return message;
    }

    @Override
    protected CollisionShape getShape()
    {
        return generateShape();
    }

    public static CollisionShape generateShape()
    {
        // Generates a collision shape for this.
        // This does the exact same thing that the superclass does, but adds a height offset
        // The parameteres for the collider are defined as the static constants in the class
        CapsuleCollisionShape capsuleCollisionShape = new CapsuleCollisionShape(_radius, (_height - (2 * _radius)));
        CompoundCollisionShape compoundCollisionShape = new CompoundCollisionShape();
        Vector3f addLocation = new Vector3f(xOffset, (_height / 2.0f) + yOffset, zOffset);
        compoundCollisionShape.addChildShape(capsuleCollisionShape, addLocation);
        return compoundCollisionShape;
    }

    public Vector3f getLocation()
    {
        return spatial.getLocalTranslation();
    }
}

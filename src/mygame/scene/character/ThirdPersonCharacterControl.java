package mygame.scene.character;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import mygame.game.Main;
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
public class ThirdPersonCharacterControl extends RigidBodyControl
                                         implements ActionListener, AnalogListener
{

    // Constants describing the movement of the character
    static final float maxSpeed = .05f, maxRotation = 5f, rotationSensitivity = -1000, cameraFollowDistance = 10, rollSpeed = .05f;
    static final int throttleInc = 1;

    // Constants describing the characteristics of the character's hitbox.
    static float _radius = .6f,_height = 3.4f, xOffset = 0, yOffset = .1f, zOffset = 0;

    private float roll = 0;

    private int throttle = 0;

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
        // Calls the rigid body constructor, which sets the mass for the physics engine
        super(0f); // mass of 0 = do not handle gravity stuff

        setCollisionShape(generateShape());

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

	inputManager.addMapping("RotL", new MouseAxisTrigger(MouseInput.AXIS_X, true));
	inputManager.addMapping("RotR", new MouseAxisTrigger(MouseInput.AXIS_X, false));
	inputManager.addMapping("RotU", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
	inputManager.addMapping("RotD", new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        inputManager.addMapping("Throttle+", new KeyTrigger(Keyboard.KEY_W));
        inputManager.addMapping("Throttle-", new KeyTrigger(Keyboard.KEY_S));

        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT), new KeyTrigger(Keyboard.KEY_SPACE));

        // Adds listeners for the action
        // This allows 'this' object to be notified when one of the above set
        // keys is pressed
        inputManager.addListener(this, "RotL", "RotR", "RotD", "RotU", "Throttle+", "Throttle-", "Shoot");
    }

    // Notifies the character controller when a button event occurs.
    @Override
    public void onAction(String action, boolean isPressed, float tpf)
    {
        switch(action)
        {
            case "Shoot":
                if(isPressed) makeBullet();
                break;
            case "Throttle+":
                throttle += throttleInc * (isPressed ? 1 : 0);
                break;
            case "Throttle-":
                throttle -= throttleInc * (isPressed ? 1 : 0);
                break;
        }
    }

    private float[] _rotation = new float[] {0, 0, 0};
    private boolean onAnalog_first = true;

    @Override
    public void onAnalog(String action, float value, float tpf)
    {
        if(onAnalog_first)
        {
            _rotation = this.getPhysicsRotation().toAngles(null);
            onAnalog_first = false;
        }

        switch(action)
        {
            case "RotL":
                _rotation[1] += value * tpf * rotationSensitivity;
                break;
            case "RotR":
                _rotation[1] -= value * tpf * rotationSensitivity;
                break;
            case "RotU":
                _rotation[0] -= value * tpf * rotationSensitivity;
                break;
            case "RotD":
                _rotation[0] += value * tpf * rotationSensitivity;
                break;
        }
    }

    // Handles movement as the game goes on.
    @Override
    public void update(float tpf)
    {
        // Has the superclass take care of physics stuff
        super.update(tpf);

        Vector3f targetLocation = this.getPhysicsLocation()
                .add(this.getPhysicsRotation().getRotationColumn(2)
                .mult((((float) throttle) / 100f) * maxSpeed));

        this.setPhysicsLocation(targetLocation);

        System.out.println(throttle);

        roll += maxRotation * (throttle / 100f) * tpf;
     }

    /**
     * Converts this object into a message to be networked
     * @return The message.
     */
    public PlayerInformationMessage toMessage()
    {
        // Constructs the message
        PlayerInformationMessage message = new PlayerInformationMessage();

        // Gets the location and rotation data from this object and puts it in the messaeg
        message.location = new float[]
            {this.getPhysicsLocation().x, this.getPhysicsLocation().y, this.getPhysicsLocation().z};
        message.rotation = new float[]
            {this.getPhysicsRotation().getX(), this.getPhysicsRotation().getY(), getPhysicsRotation().getZ(), getPhysicsRotation().getW()};
        message.currentSpeed = (throttle * maxSpeed) / 100f;

        // Tells the message to use UDP rather than TCP protocol
        // TCP -> slow, reliable (no packet loss)
        // UDP -> fast, unreliable (packet loss, messages may appear in the wrong order)
        message.setReliable(false);
        // Returns the message
        return message;
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

    int bulletNum = 0;
    public void makeBullet()
    {
        Vector3f size = new Vector3f(.1f, .05f, .4f);

        Geometry bullet = new Geometry("bullet" + bulletNum++, new Box(size.x, size.y, size.z));
        bullet.setLocalTranslation(this.getPhysicsLocation().add(0, .75f, 0));

        Material mat = new Material(Main.instance.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);

        bullet.setMaterial(mat);

        BulletControl bulletControl = new BulletControl(new BoxCollisionShape(size),
                this.getPhysicsRotation().getRotationColumn(2).normalize().mult(0.006f + (((throttle > 0 ? throttle : 0) * maxSpeed ) / 100f)),
                5f);

        bullet.setLocalRotation(this.getPhysicsRotation());

        Main.instance.addSpatial(bullet, bulletControl);
    }
}

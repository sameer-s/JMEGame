package mygame.scene.character;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import mygame.game.Main;
import org.lwjgl.input.Keyboard;

/**
 * A class to control the ship.
 *
 * @author Sameer Suri
 */
public class ShipCharacterControl extends RigidBodyControl implements ActionListener
{

    // Constants describing the movement of the character
    static final float maxSpeed = .05f;
    static final int throttleInc = 1;

    // Constants describing the characteristics of the character's hitbox.
    static float _radius = .6f,_height = 3.4f, xOffset = 0, yOffset = .1f, zOffset = 0;

    private int throttle = 0;

    // The instance of the JME camera class that we use to find out which way the player is looking.
    @SuppressWarnings("FieldMayBeFinal")
    protected Camera cam;

    /**
     * Constructor for the control.
     * @param spatial The model used, so that the control can find the animations in the model
     * @param cam The camera, so that the control can see where the player is looking
     */
    public ShipCharacterControl(Spatial spatial, Camera cam)
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
     * @return This object.
     */
    public ShipCharacterControl initKeys(InputManager inputManager)
    {
        // Binds keys to their respective actions

        inputManager.addMapping("Throttle+", new KeyTrigger(Keyboard.KEY_W));
        inputManager.addMapping("Throttle-", new KeyTrigger(Keyboard.KEY_S));

        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT), new KeyTrigger(Keyboard.KEY_SPACE));

        // Adds listeners for the action
        // This allows 'this' object to be notified when one of the above set
        // keys is pressed
        inputManager.addListener(this, "Throttle+", "Throttle-", "Shoot");
        
        return this;
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

    // Handles movement as the game goes on.
    @Override
    public void update(float tpf)
    {
        // Has the superclass take care of physics stuff
        super.update(tpf);

        Vector3f targetLocation = this.spatial.getLocalTranslation()
                .add(this.spatial.getLocalRotation().getRotationColumn(2)
                .mult((((float) throttle) / 100f) * maxSpeed));

        this.spatial.setLocalTranslation(targetLocation);

        System.out.println(throttle);
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
        bullet.setLocalTranslation(this.spatial.getLocalTranslation().add(0, -1.5f, 0));

        Material mat = new Material(Main.instance.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);

        bullet.setMaterial(mat);

        BulletControl bulletControl = new BulletControl(new BoxCollisionShape(size),
                this.spatial.getLocalRotation().getRotationColumn(2).normalize().mult(0.010f + (((throttle > 0 ? throttle : 0) * maxSpeed ) / 100f)),
                5f);

        bullet.setLocalRotation(this.spatial.getLocalRotation());

        Main.instance.addSpatial(bullet, bulletControl);
    }
}

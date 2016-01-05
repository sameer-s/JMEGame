package mygame.character;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.CameraInput;
import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import mygame.network.message.PlayerInformationMessage;

/**
 * A class to control the third person character.
 * Although some aspects of this class are specific to this game, I have
 * tried to create it so that it would not be difficult to port to a
 * different game.
 * @author Sameer Suri
 */
public class ThirdPersonCharacterControl extends BetterCharacterControl
                                         implements ActionListener
{
    // Booleans tracking which direction the player is moving
    // true -> "yes, the player is moving in that direction"
    // false -> "no, the player is not moving in that direction"
    // Multiple of these can be true at any given time.
    private boolean forward, backward, left, right;

    // Constants describing the movement of the character
    static final float moveSpeed = 10f, jumpBoost = 1f;

    // Constants describing the physical characteristics of the character
    public static final float _radius = .6f, _height = 3.4f, heightOffset = .1f, _mass = 1f;

    // The instance of the JME camera class that we use to find out which way the player is looking.
    @SuppressWarnings("FieldMayBeFinal")
    private Camera cam;

    // A field for mapping Strings to animation channels. This is useful because
    // the Mixamo tool generates a number of different animation controls
    // and animation channels for different parts of the body (see the bodyNodes
    // constant). Due to this, it is easiest to just abstract it a bit, and map
    // each channel to a name.
    @SuppressWarnings("FieldMayBeFinal")
    private HashMap<String, AnimChannel> animChannels = new HashMap<>();
    // A field for mapping names to the names of animations (which may vary
    // from model to model) This way, it is easy to add new animations; all you
    // have to do is add a new entry to the Map.
    @SuppressWarnings("FieldMayBeFinal")
    private HashMap<String, String> animations;

    // The array that stores the names that Mixamo uses for each body part.
    static final String[] bodyNodes = new String[]
    {
        "Beards",
        "Body",
        "Bottoms",
        "Eyelashes",
        "Eyes",
        "Hair",
        "Moustaches",
        "Shoes",
        "Tops"
    };


    /**
     * Constructor for the control.
     * @param animations The map of animations, so the control knows which ones to use
     * @param spatial The model used, so that the control can find the animations in the model
     * @param cam The camera, so that the control can see where the player is looking
     */
    public ThirdPersonCharacterControl(HashMap<String, String> animations, Spatial spatial, Camera cam)
    {
        // Calls the BetterCharacterControl constructor, which, among other things
        // creates a Bullet Physics entity to the player, given a radius, height,
        // and mass (this would be represented as a capsule shape)
        super(_radius, _height, _mass);

        System.out.printf("Spatial is: Node ? %b | Geometry ? %b\n", spatial instanceof Node, spatial instanceof Geometry);

        // Increases the jump force depending on how much it is boosted in the
        // corresponding constant.
        this.setJumpForce(this.getJumpForce().mult(jumpBoost));

        // Stores the animations map in an instance variable
        this.animations = animations;

        // This loops over each of the body parts that are created with the
        // Mixamo FUSE tool for creating characters
        for(String bodyNode : bodyNodes)
        {
            // Gets the Spatial corresponding to that String
            Spatial bodyPart = ((Node) spatial).getChild(bodyNode);
            // Finds the animation controller put in by Mixamo
            AnimControl control = bodyPart.getControl(AnimControl.class);

            // Creates a new animation channel so that we can use it to set
            // animations later, and stores it in our HashMap
            animChannels.put(bodyNode, control.createChannel());
        }

        // Stores the camera in an instance variable
        this.cam = cam;

        // Sets the current animation to the idle animation
        setAnim("Idle");
    }

    /**
     * Binds keys to their respective actions.
     * Also takes care of joystick input.
     * @param inputManager The input manager that is uesd for binding the actions.
     * @param joystickId The ID of the joystick. FOR DEBUGGING, TO BE REMOVED IN FINAL RELEASE.
     */
    public void initKeys(InputManager inputManager, int joystickId)
    {
        // Binds keys to their respective actions
        // For now, there is movement and jumping

        // This one binds forward movement to the W key
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        // This one binds left movement to the A key
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        // This one binds backward movement to the S key
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        // This one binds right movement to the D key
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));

        // This one binds jumping to the spacebar
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

        // Joystick bindings

        // If the joystick does not go more than halfway through, don't move.
        // This is to avoid calibration errors.
        inputManager.setAxisDeadZone(0.5f);

        // Gets a list of the current joysticks.
        Joystick[] joysticks = inputManager.getJoysticks();

        // If there are any joysticks...
        if(joysticks != null)
        {
            // ... for each joystick ...
            for(Joystick joystick : joysticks)
            {
                // ... if its ID matches the provided one ...
                if(joystick.getJoyId() == joystickId)
                {
                    // ... bind its axes to movement.
                    joystick.getAxis(JoystickAxis.X_AXIS).assignAxis("Right", "Left");
                    joystick.getAxis(JoystickAxis.Y_AXIS).assignAxis("Backward", "Forward");

                    joystick.getButton(JoystickButton.BUTTON_2).assignButton("Jump");
                    joystick.getButton(JoystickButton.BUTTON_0).assignButton("Disco");
                    joystick.getButton(JoystickButton.BUTTON_1).assignButton("Debug");

                    joystick.getAxis(JoystickAxis.Z_AXIS).assignAxis(CameraInput.CHASECAM_MOVERIGHT, CameraInput.CHASECAM_MOVELEFT);
                    joystick.getAxis(JoystickAxis.Z_ROTATION).assignAxis(CameraInput.CHASECAM_DOWN, CameraInput.CHASECAM_UP);
                }
            }
        }

        // Adds listeners for the action
        // This allows 'this' object to be notified when one of the above set
        // keys is pressed
        inputManager.addListener(this, "Forward", "Left", "Backward", "Right", "Jump");
    }

    // Notifies the character controller when a button event occurs.
    @Override
    public void onAction(String action, boolean isPressed, float tpf)
    {
        // What was the name of the button event?
        switch (action)
        {
            // If it is a movement event, update the corresponding boolean
            // isPressed = true -> press event
            // isPressed = false -> release event
            case "Forward":
                forward = isPressed;
                break;
            case "Left":
                left = isPressed;
                break;
            case "Backward":
                backward = isPressed;
                break;
            case "Right":
                right = isPressed;
                break;
            // If it's a jump event, jump,.
            case "Jump":
                jump();
                break;
            // If it's a duck event (not yet implemented), duck.
            case "Duck":
                setDucked(isPressed);
                break;
           /* Note: jump and duck are methods created in the superclass */
        }
    }

    // Contains the direction that is currently 'forward', for the character
    Vector3f forwardVector = new Vector3f();

    // Handles movement as the game goes on.
    @Override
    public void update(float tpf)
    {
        // Has the superclass take care of some stuff
        super.update(tpf);

        // Gets the current direction of the CAMERA
        Vector3f modelForwardDir = cam.getRotation().mult(Vector3f.UNIT_Z).multLocal(1, 0, 1);
        Vector3f modelLeftDir = cam.getRotation().mult(Vector3f.UNIT_X);

        // walkDirection keeps track of the direction that the player walks in
        walkDirection.set(0, 0, 0);
        if (forward)
        {
            // If it is going forward, go in the forward direction (times the speed)
            walkDirection.addLocal(modelForwardDir.mult(moveSpeed));

            // Then set the current forward direction as that.
            forwardVector.set(walkDirection);
         }
        else if (backward)
        {
            // If it is going backward, go in the negative forward direction (times the speed)
            walkDirection.addLocal(modelForwardDir.negate().multLocal(moveSpeed));

            // Then set the current forward direction as that.
            forwardVector.set(walkDirection);
        }
        if (left)
        {
            // If it is going left, go in the left direction (times the speed)
            walkDirection.addLocal(modelLeftDir.mult(moveSpeed));

            // Then set the current forward direction as that.
            forwardVector.set(walkDirection);
        }
        else if (right)
        {
            // If it is going right, go in the negative left direction (times the speed)
            walkDirection.addLocal(modelLeftDir.negate().multLocal(moveSpeed));

            // Then set the current forward direction as that.
            forwardVector.set(walkDirection);
        }

        // Handle the movement animations
        handleAnimations();

        walkDirection.normalizeLocal();
        walkDirection.multLocal(moveSpeed);
        walkDirection.setY(0f);

        // Set the current direction that we are looking at as the "forward" direction
        viewDirection.set(forwardVector);
        // Does the same thing for the physics engine
        setViewDirection(forwardVector);
    }

    // Handles animations on the player
    // To be called in an update loop (from the update method)
    private void handleAnimations()
    {
        // If we are moving...
        if(forward || backward || left || right)
        {
            // ... but we are not currently in the moving animation...
            if(!getAnim().equals("Move"))
            {
                // ... switch to the moving animation.
                setAnim("Move");
            }
        }
        // On the other hand, if we are not moving...
        else
        {
            // ... but we are not currently in the idle animation...
            if(!getAnim().equals("Idle"))
            {
                // ... switch to the idle animation.
                setAnim("Idle");
            }
        }
    }

    // A few different overloads of a method that do the same thing:
    // set the current animation.

    /**
     * Sets the current animation.
     * The loop mode will stay the same, and the animation will be applied
     * to all channels.
     * @param anim The animation to switch to
     */
    private void setAnim(String anim)
    {
        setAnim(anim, (LoopMode) null);
    }

    /**
     * Sets the current animation.
     * The loop mode will stay the same.
     * @param anim The animation to switch to
     * @param channels The animation channels to apply this to
     */
    private void setAnim(String anim, String... channels)
    {
        setAnim(anim, null, channels);
    }

    /**
     * Sets the current animation.
     * @param anim The animation to switch to.
     * @param loopMode The loop mode (i.e. loop, not loop).
     * @param channels The animation channels to apply this to. This argument is optional, and if not passed, will automatically assume ALL channels.
     */
    private void setAnim(String anim, LoopMode loopMode, String... channels)
    {
        // If the length of the channels array is 0 (the parameter was not passed)...
        if(channels.length == 0)
        {
            // ... set it as ALL of the animations.
            channels = animChannels.keySet().toArray(new String[0]);
        }
        // For each animation channel [name]
        for(String channel : channels)
        {
            // Sets the animation for the object
            // Gives it 0.3 seconds to blend the animation
            animChannels.get(channel).setAnim(animations.get(anim), 0.3f);
            // If the loopmode is null, don't change the loop mode
            if(loopMode != null)
            {
                // If it isn't null, change it to whatever is passed.
                animChannels.get(channel).setLoopMode(loopMode);
            }
        }
    }

    /**
     * Gets the current animation.
     * It does this by checking the current animation on the first element of
     * the animation channel, and is not necessarily accurate for all channels
     * @return The name of the current animation
     */
    private String getAnim()
    {
        // Calls the other getAnim method with the first value in the
        // set of all animation channels.
        return getAnim(animChannels.keySet().iterator().next());
    }

    /**
     * Gets the current animation for a given animation channel.
     * @param channel The name of the animation channel to check.
     * @return The name of the current animation
     */
    private String getAnim(String channel)
    {
        // Gets the name (as registered in the model) of the current animation
        String anim = animChannels.get(channel).getAnimationName();
        // Creates a variable to store the corresponding key (the name of the animation in our naming system)
        String animKey = anim;

        // For each possible animation key
        for(String key : animations.keySet())
        {
            // If its corresponding animation matches the one that is currently running
            if(anim.equals(animations.get(key)))
            {
                // Set that as the correct key; break out of the for loop
                animKey = key;
                break;
            }
        }

        // Return the animation key provided to you
        return animKey;
    }

    /**
     * Converts this object into a message to be networked.
     * @return The message.
     */
    public PlayerInformationMessage toMessage()
    {
        // Constructs the message
        PlayerInformationMessage message = new PlayerInformationMessage();

        // Initializes the array that holds the number of
        message.currentAnims = new String[bodyNodes.length];
        for(int i = 0; i < bodyNodes.length; i++)
        {
            message.currentAnims[i] = getAnim(bodyNodes[i]);
        }

        // Gets the location and rotation data from this object and puts it in the messaeg
        message.location = new float[]
            {location.x, location.y, location.z};
        message.rotation = new float[]
            {rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW()};

        // Tells the message to use UDP rather than TCP protocol
        // TCP -> slow, reliable (no packet loss)
        // UDP -> fast, unreliable (packet loss)
        message.setReliable(false);
        // Returns the message
        return message;
    }

    @Override
    protected CollisionShape getShape()
    {
        // Does the exact same as the superclass implementation but adds a height offset
        CapsuleCollisionShape capsuleCollisionShape = new CapsuleCollisionShape(getFinalRadius(), (getFinalHeight() - (2 * getFinalRadius())));
        CompoundCollisionShape compoundCollisionShape = new CompoundCollisionShape();
        Vector3f addLocation = new Vector3f(0, (getFinalHeight() / 2.0f) + heightOffset, 0);
        compoundCollisionShape.addChildShape(capsuleCollisionShape, addLocation);
        return compoundCollisionShape;
    }
}

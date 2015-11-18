package mygame;

import com.jme3.animation.*;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;

import mygame.ThirdPersonCharacter.ThirdPersonCamera.CameraProperties;

/**
 * A class to control a "3rd person character". Adapted from the tutorial.
 *
 * @author Sameer
 *
 */
public class ThirdPersonCharacter extends Node
        implements ActionListener, AnalogListener
{
    private boolean forward = false, backward = false, left = false,
            right = false;
    private BetterCharacterControl control;
    private ThirdPersonCamera cam;
    private AnimChannel animChannel;
    // The current walk direction of th
    private Vector3f walkDirection = new Vector3f();

    /* USER DEFINED PROPERTIES */
    // Variables that contain the names of the animations used
    public Animations anims;
    // Variables that describe the speed used for different actions
    public Movement movement;
    // Variables that describe the size and physics mass of the player object
    public SpatialProperties properties;

    /**
     * Constructs a third person character using ALL user defined properties
     *
     * @param model The 3d model of the character
     * @param inputManager The input manager to initialize key presses
     * @param cam The camera to attach to the player
     * @param anims The animations for the player to use
     * @param movement The movement properties for the player to use
     * @param properties The spatial properties for the player to use
     * @param cameraProperties The properties for the camera to use
     */
    public ThirdPersonCharacter(Spatial model, InputManager inputManager,
            Camera cam, Animations anims, Movement movement,
            SpatialProperties properties, CameraProperties cameraProperties)
    {
        // Invokes the constructor of 'Node'
        super();

        // Initializes the user-defined properties
        this.anims = anims;
        this.movement = movement;
        this.properties = properties;

        // Sets up key presses
        initKeys(inputManager);

        // Sets up the character control
        control = new BetterCharacterControl(this.properties.radius,
                this.properties.height, this.properties.mass);
        control.setJumpForce(this.movement.jumpForce);
        addControl(control);

        // Attaches the model to be used with this object
        attachChild(model);

        // Initializes the animations to be used
        AnimControl animControl = model.getControl(AnimControl.class);
        animChannel = animControl.createChannel();
        animChannel.setAnim(this.anims.idleAnim);
        // Attaches the camera object to 'this'
        this.cam = new ThirdPersonCamera("CamNode", cam, this,
                cameraProperties);
    }

    /**
     * Constructs a third person character with default 'user-defined'
     * properties
     *
     * @param model The 3d model of the character
     * @param inputManager The input manager to initialize key presses
     * @param cam The camera to attach to the player
     */
    public ThirdPersonCharacter(Spatial model, InputManager inputManager,
            Camera cam)
    {
        this(model, inputManager, cam, Animations.DEFAULT, Movement.DEFAULT,
                SpatialProperties.DEFAULT, CameraProperties.DEFAULT);
    }

    /**
     * Initializes the keys for use with the character
     *
     * @param inputManager The input manager to use for initializing key
     * presses.
     */
    private void initKeys(InputManager inputManager)
    {
        // Binds keys to their respective actions
        // For example, there is movement, jumping and turning

        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("TurnLeft",
                new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("TurnRight",
                new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouseLookDown",
                new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("MouseLookUp",
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("Camera", new KeyTrigger(KeyInput.KEY_C));

        // Adds listeners for the action
        // This allows 'this' object to be notified when one of the above set
        // keys is pressed
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Forward");
        inputManager.addListener(this, "Backward");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "TurnLeft");
        inputManager.addListener(this, "TurnRight");
        inputManager.addListener(this, "MouseLookDown");
        inputManager.addListener(this, "MouseLookUp");
        inputManager.addListener(this, "Camera");

        System.out.println("Added Mappings");
    }

    /**
     * Handles the "action" (digital) events; i.e. keyboard or mouse button
     * presses (or releases).
     *
     * @param binding The name of the key binding being changed
     * @param value The value, is it pressed or not?
     * @param tpf 'Time per frame', the time taken between each frame.
     */
    @Override
    public void onAction(String binding, boolean value, float tpf)
    {
        // Checks what key was pressed or released
        if (binding.equals("Left"))
        {
            // Left was pressed or released, update its flag accordingly
            left = value;
        } else
        {
            if (binding.equals("Right"))
            {
                // Right was pressed or released, update its flag accordingly
                right = value;
            } else
            {
                if (binding.equals("Forward"))
                {
                    // Forward was pressed or released, update its flag accordingly
                    forward = value;
                } else
                {
                    if (binding.equals("Backward"))
                    {
                        // Backward was pressed or released, update its flag accordingly
                        backward = value;
                    } else
                    {
                        if (binding.equals("Jump"))
                        {
                            // Jump was pressed; jump if not on ground
                            if (control.isOnGround())
                            {
                                control.jump();
                            }
                        } else
                        {
                            if (binding.equals("Camera") && value == true)
                            {
                                // Toggle the camera view
                                cam.getCameraNode().setLocalTranslation(new Vector3f(0, 0,
                                        -cam.getCameraNode().getLocalTranslation().z));
                                cam.getCameraNode().rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles the "analog" events; i.e. mouse axis movements.
     *
     * @param binding The name of the key binding being changed
     * @param value The analog value
     * @param tpf 'Time per frame', the time taken between each frame.
     */
    @Override
    public void onAnalog(String binding, float value, float tpf)
    {
        if (binding.equals("TurnLeft"))
        {
            Quaternion turn = new Quaternion();
            turn.fromAngleAxis(movement.mouseLookSpeed * value,
                    Vector3f.UNIT_Y);
            control.setViewDirection(turn.mult(control.getViewDirection()));
        } else
        {
            if (binding.equals("TurnRight"))
            {
                Quaternion turn = new Quaternion();
                turn.fromAngleAxis(-movement.mouseLookSpeed * value,
                        Vector3f.UNIT_Y);
                control.setViewDirection(turn.mult(control.getViewDirection()));
            } else
            {
                if (binding.equals("MouseLookDown"))
                {
                    cam.verticalRotate(movement.mouseLookSpeed * value);
                } else
                {
                    if (binding.equals("MouseLookUp"))
                    {
                        cam.verticalRotate(-movement.mouseLookSpeed * value);
                    }
                }
            }
        }
    }

    /**
     * Handles those animations that need extra handling
     *
     * @throws NullPointerException If the animations don't exist
     */
    private void handleAnimations() throws NullPointerException
    {
        if (control.isOnGround())
        {
            if (left || right || forward || backward)
            {
                if (!animChannel.getAnimationName().equals(anims.moveAnim))
                {
                    animChannel.setAnim(anims.moveAnim, .3f);
                    animChannel.setLoopMode(LoopMode.Loop);
                }
            } else
            {
                if (!animChannel.getAnimationName().equals(anims.idleAnim))
                {
                    animChannel.setAnim(anims.idleAnim, .3f);
                    animChannel.setLoopMode(LoopMode.Cycle);
                }
            }
        }
    }

    /**
     * To be called from the main 'simpleUpdate' method Updates the camera and
     * movement; handles animations
     */
    public void update()
    {
        Vector3f camDir = cam.getCameraNode().getCamera().getDirection()
                .clone();
        camDir.y = 0;
        Vector3f camLeft = cam.getCameraNode().getCamera().getLeft().clone();
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);

        if (left)
        {
            walkDirection.addLocal(camLeft);
        }
        if (right)
        {
            walkDirection.addLocal(camLeft.negate());
        }
        if (forward)
        {
            walkDirection.addLocal(camDir);
        }
        if (backward)
        {
            walkDirection.addLocal(camDir.negate());
        }

        control.setWalkDirection(
                walkDirection.normalize().multLocal(movement.walkSpeed));

        try
        {
            // handleAnimations(); TODO
        } catch (NullPointerException npe)
        {
            npe.printStackTrace(System.err);
        }
    }

    /**
     * Gets the BetterCharacterControl controller object associated with this
     * object
     */
    public BetterCharacterControl getControl()
    {
        return control;
    }

    /**
     * A class to control which animations are played
     *
     * @author Sameer
     *
     */
    public static class Animations
    {
        public String idleAnim, moveAnim;

        public Animations(String idleAnim, String moveAnim)
        {
            this.idleAnim = idleAnim;
            this.moveAnim = moveAnim;
        }
        public static final Animations DEFAULT = new Animations("Idle", "Walk");
    }

    /**
     * A class to control the movement of the 3rd person character
     *
     * @author Sameer
     *
     */
    public static class Movement
    {
        public Vector3f jumpForce = new Vector3f(0f, 8.5f, 0f);
        public float walkSpeed = 8f, mouseLookSpeed = 3.15f;

        public Movement(Vector3f jumpForce, float walkSpeed,
                float mouseLookSpeed)
        {
            this.jumpForce = jumpForce;
            this.walkSpeed = walkSpeed;
            this.mouseLookSpeed = mouseLookSpeed;
        }
        public static final Movement DEFAULT = new Movement(
                new Vector3f(0f, 8.5f, 0f), 8f, 3.15f);
    }

    /**
     * A class to control the size and mass of the 3rd person character
     *
     * @author Sameer
     *
     */
    public static class SpatialProperties
    {
        public float radius, height, mass;

        public SpatialProperties(float radius, float height, float mass)
        {
            this.radius = radius;
            this.height = height;
            this.mass = mass;
        }
        public static final SpatialProperties DEFAULT = new SpatialProperties(
                .5f, 1f, 1f);
    }

    /**
     * A camera that automatically follows the player.
     *
     * @author Sameer
     *
     */
    public static class ThirdPersonCamera
    {
        // An invisible 'pivot' node, so that the player does not rotate when we
        // are looking around
        private Node pivot;
        // The actual camera
        private CameraNode cameraNode;
        public CameraProperties properties;

        /**
         * Creates a third person camera
         *
         * @param name The name of the third person camera object
         * @param cam The actual camera object
         * @param player The player to follow
         * @param properties The properties of the camera
         */
        public ThirdPersonCamera(String name, Camera cam, Node player,
                CameraProperties properties)
        {
            // Sets the camera properties
            this.properties = properties;

            // Creates the pivot node
            pivot = new Node("PivotNode");
            player.attachChild(pivot);

            // Creates the camera node
            cameraNode = new CameraNode(name, cam);

            // Makes the camera follow the player
            cameraNode.setControlDir(
                    CameraControl.ControlDirection.SpatialToCamera);

            // Attaches the camera to the player
            pivot.attachChild(cameraNode);

            // Keeps the camera away from the player
            cameraNode.setLocalTranslation(
                    new Vector3f(0, 0, this.properties.followDistance));

            // Makes the camera look at the player
            cameraNode.lookAt(pivot.getLocalTranslation(), Vector3f.UNIT_Y);

            // Sets the camera's rotation as the default
            pivot.getLocalRotation().fromAngleAxis(
                    -this.properties.verticalAngle, Vector3f.UNIT_X);
        }

        /**
         * Rotates the camera
         *
         * @param angle How much to rotate by
         */
        public void verticalRotate(float angle)
        {
            // Adds the new angle to the vertical angle
            properties.verticalAngle += angle;

            // Makes sure the angle is within the bounds
            if (properties.verticalAngle > properties.maxVerticalAngle)
            {
                properties.verticalAngle = properties.maxVerticalAngle;
            } else
            {
                if (properties.verticalAngle < properties.minVerticalAngle)
                {
                    properties.verticalAngle = properties.minVerticalAngle;
                }
            }

            // Adjusts the camera accordingly
            pivot.getLocalRotation().fromAngleAxis(-properties.verticalAngle,
                    Vector3f.UNIT_X);
        }

        /**
         * Gets the camera node
         *
         * @return The camera node
         */
        public CameraNode getCameraNode()
        {
            return cameraNode;
        }

        /**
         * Gets the object that the camera is tracking
         *
         * @return The pivot node, which the camera is tracking
         */
        public Node getCameraTrack()
        {
            return pivot;
        }

        public static class CameraProperties
        {
            // The distance to follow the player at
            public float followDistance, verticalAngle, maxVerticalAngle,
                    minVerticalAngle;

            /**
             * Creates the camera properties object
             *
             * @param followDistance The distance to follow behind the player
             * @param verticalAngle The default vertical angle for the camera,
             * in degrees
             * @param maxVerticalAngle The maximum vertical angle the camera can
             * go, in degrees
             * @param minVerticalAngle The minimum vertical angle the camera can
             * go, in degrees
             */
            public CameraProperties(float followDistance, float verticalAngle,
                    float maxVerticalAngle, float minVerticalAngle)
            {
                this.followDistance = followDistance;
                this.verticalAngle = verticalAngle * FastMath.DEG_TO_RAD;
                this.maxVerticalAngle = maxVerticalAngle * FastMath.DEG_TO_RAD;
                this.minVerticalAngle = minVerticalAngle * FastMath.DEG_TO_RAD;
            }
            public static final CameraProperties DEFAULT = new CameraProperties(
                    7, 30, 90, 10);
        }
    }
}

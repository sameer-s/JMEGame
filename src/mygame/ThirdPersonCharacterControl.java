package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
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
import java.util.HashMap;

/**
 * A class to control a third person character.
 * @author ssuri
 */
public class ThirdPersonCharacterControl extends BetterCharacterControl
                                         implements ActionListener, AnalogListener
{
    private boolean forward, backward, left, right;
    private final Node head;
    private float yaw = 0;
    
    private static final float moveSpeed = 10f, sensitivity = 15f, jumpBoost = 2;
    
    private static final float _radius = .5f, _height = 1f, _mass = 1f;
    
    private static final float cameraFollowDistance = 3.5f;

    private HashMap<String, AnimChannel> animChannels = new HashMap<>();
    public HashMap<String, String> animations;

    private static final String[] bodyNodes = new String[]
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
    
    public ThirdPersonCharacterControl(InputManager man,
            HashMap<String, String> animations, Spatial spatial)
    {
        super(_radius, _height, _mass);
        head = new Node();
        head.setLocalTranslation(0, 1.8f, 0f);
        initKeys(man);
        this.setJumpForce(this.getJumpForce().mult(jumpBoost));
        this.animations = animations;
        
        for(String bodyNode : bodyNodes)
        {
            Spatial bodyPart = ((Node) spatial).getChild(bodyNode);
            AnimControl control = bodyPart.getControl(AnimControl.class);
            
            switch(bodyNode)
            {
                case "Bottoms":
                    int numBones = control.getSkeleton().getBoneCount();
                    for(int i = 0; i < numBones; i++)
                    {
                        System.out.println("Bone: " + control.getSkeleton().getBone(i).getName());
                    }
                    //break;
                default:
                    animChannels.put(bodyNode, control.createChannel());
                    break;
                    
            }
          
        }
        
        setAnim("Idle");
    }
    
   private void initKeys(InputManager inputManager)
    {
        // Binds keys to their respective actions
        // For example, there is movement, jumping and turning

        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("LookLeft",
                new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("LookRight",
                new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("LookDown",
                new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("LookUp",
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
        inputManager.addListener(this, "LookLeft");
        inputManager.addListener(this, "LookRight");
        inputManager.addListener(this, "LookDown");
        inputManager.addListener(this, "LookUp");
        inputManager.addListener(this, "Camera");
    }
   
    @Override
    public void onAction(String action, boolean isPressed, float tpf) 
    {
        switch (action)
        {
            case "Left":
                left = isPressed;
                break;
            case "Right":
                right = isPressed;
                break;
            case "Forward":
                forward = isPressed;
                break;
            case "Backward":
                backward = isPressed;
                break;
            case "Jump":
                jump();
                break;
            case "Duck":
                setDucked(isPressed);
                break;
        }
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf)
    {
        switch (name)
        {
            case "LookLeft":
                rotate(tpf * value * sensitivity);
                break;
            case "LookRight":
                rotate(-tpf * value * sensitivity);
                break;
            case "LookUp":
                lookUpDown(-value * tpf * sensitivity);
                break;
            case "LookDown":
                lookUpDown(value * tpf * sensitivity);
                break;
        }
    }
    
    private void rotate(float value)
    {
        Quaternion rotate = new 
        Quaternion().fromAngleAxis(FastMath.PI * value, Vector3f.UNIT_Y);
        rotate.multLocal(viewDirection);
        setViewDirection(viewDirection);
    }
    
    private void lookUpDown(float value)
    {
        yaw += value;
        yaw = FastMath.clamp(yaw, -FastMath.HALF_PI, FastMath.HALF_PI);
        head.setLocalRotation(new Quaternion().fromAngles(yaw, 0, 0));
    }
    
    @Override
    public void update(float tpf)
    {
        super.update(tpf);
        Vector3f modelForwardDir = spatial.getWorldRotation().mult(Vector3f.UNIT_Z);
        Vector3f modelLeftDir = spatial.getWorldRotation().
        mult(Vector3f.UNIT_X);
        walkDirection.set(0, 0, 0);
        if (forward)
        {
            walkDirection.addLocal(modelForwardDir.mult(moveSpeed));
        }
        else if (backward)
        {
            walkDirection.addLocal(modelForwardDir.negate().
            multLocal(moveSpeed));
        }   
        if (left)
        {
            walkDirection.addLocal(modelLeftDir.mult(moveSpeed));
        }
        else if (right)
        {
            walkDirection.addLocal(modelLeftDir.negate().
            multLocal(moveSpeed));
        }
        
        if(forward || backward || left || right)
        {
            if(!getAnim("Bottoms").equals("Move"))
//            if(!getAnim().equals("Move"))
            {
                setAnim("Move", "Bottoms", "Shoes");
//                setAnim("Move");
            }
        }
        else
        {
            if(!getAnim("Bottoms").equals("Idle"))
//            if(!getAnim().equals("Idle"))
            {
//                setAnim("Idle", "Bottoms", "Shoes");
                setAnim("Idle");
            }
        }
        
        
    }
    
    @Override
    public void setSpatial(Spatial spatial)
    {
        super.setSpatial(spatial);
        if(spatial instanceof Node)
        {
            ((Node) spatial).attachChild(head);
        }
    }
    
    public void setCamera(Camera cam)
    {
        CameraNode camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        head.attachChild(camNode);
        
        // Third Person Stuff
        camNode.setLocalTranslation(new Vector3f(0, cameraFollowDistance, -cameraFollowDistance));
        camNode.lookAt(head.getLocalTranslation(), Vector3f.UNIT_Y);
    }
    
    private void setAnim(String anim)
    {
        setAnim(anim, null, animChannels.keySet().toArray(new String[0]));
    }
    
    private void setAnim(String anim, String... channels)
    {
        setAnim(anim, null, channels);
    }
    
    private void setAnim(String anim, LoopMode loopMode)
    {
        setAnim(anim, loopMode, animChannels.keySet().toArray(new String[0]));
    }
    
    private void setAnim(String anim, LoopMode loopMode, String... channels)
    {
        if(channels.length == 0)
        {
            channels = animChannels.keySet().toArray(new String[0]);
        }
        
        for(String channel : channels)
        {
            animChannels.get(channel).setAnim(animations.get(anim));
            if(loopMode != null)
            {
                animChannels.get(channel).setLoopMode(loopMode);
            }
        }
    }
    
    private String getAnim()
    {
        return getAnim(animChannels.keySet().iterator().next());
    }
    private String getAnim(String channel)
    {
        String anim = animChannels.get(channel).getAnimationName();
        String animKey = anim;
        
        for(String key : animations.keySet())
        {
            if(anim.equals(animations.get(key)))
            {
                animKey = key;
                break;
            }
        }
            
        return animKey;
    }
}

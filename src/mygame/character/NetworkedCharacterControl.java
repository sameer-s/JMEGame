package mygame.character;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import mygame.network.message.PlayerInformationMessage;

/**
 * A controller for a character whose location is being updated over network.
 * It is updated by way of PlayerInformationMessage.
 * @author Sameer Suri
 */
public class NetworkedCharacterControl extends RigidBodyControl
{
    public NetworkedCharacterControl()
    {
        // Uses the character's mass for the physics engine
        super(ThirdPersonCharacterControl._mass);
        // Sets up the collider shape as the character does
        setCollisionShape(ThirdPersonCharacterControl.generateShape());
        // Essentially allows this model to move
        setKinematic(true);
    }

    private PlayerInformationMessage m;

    public void recieveMessage(PlayerInformationMessage m)
    {
        this.m = m;
    }

    @Override
    public void update(float tpf)
    {
        super.update(tpf);

        if(m == null) return;

        Vector3f i = this.spatial.getLocalTranslation();
        Vector3f j = this.getPhysicsLocation();
        Vector3f k = new Vector3f(m.location[0], m.location[1], m.location[2]);

        move(m, tpf);
        rotate(m);
        animate(m);

        System.out.println("" + i + j + k + currentLocation + this.spatial.getLocalTranslation() + this.getPhysicsLocation());
    }

    private Vector3f targetLocation;
    private Vector3f currentLocation = new Vector3f();
    private boolean firstUpdate = true;
    private void move(PlayerInformationMessage m, float tpf)
    {
        targetLocation = new Vector3f(m.location[0], m.location[1], m.location[2]);

        if(firstUpdate == true)
        {
            currentLocation = new Vector3f(targetLocation);
            firstUpdate = false;
        }
        else
        {
            final float distance = currentLocation.distance(targetLocation);
            if(distance != 0)
            {
                final Vector3f movement = targetLocation.subtract(currentLocation);
                final Vector3f adjusted = movement.mult((tpf * ThirdPersonCharacterControl.moveSpeed) / distance);

                currentLocation = adjusted.length() > movement.length() ? movement.add(currentLocation) : adjusted.add(currentLocation);
            }
        }

        // Try this one if the bottom one doesnt work
//         this.setPhysicsLocation(currentLocation);

        System.out.println(currentLocation);
        this.spatial.setLocalTranslation(currentLocation);
    }

    private Quaternion currentRotation;
    private void rotate(PlayerInformationMessage m)
    {
        currentRotation = new Quaternion(m.rotation[0], m.rotation[1], m.rotation[2], m.rotation[3]);

        this.spatial.setLocalRotation(currentRotation);
    }

    private AnimChannel[] channels;
    private HashMap<String, String> nameMap;

    public void initAnimations(HashMap<String, String> nameMap)
    {
        if(!ThirdPersonCharacterControl.animate) return;
        
        channels = new AnimChannel[ThirdPersonCharacterControl.bodyNodes.length];

        for(int i = 0; i < ThirdPersonCharacterControl.bodyNodes.length; i++)
        {
            Spatial bodyPart = ((Node) spatial).getChild(ThirdPersonCharacterControl.bodyNodes[i]);

            channels[i] = bodyPart.getControl(AnimControl.class).createChannel();
        }

        this.nameMap = nameMap;
    }

    private void animate(PlayerInformationMessage m)
    {
        if(!ThirdPersonCharacterControl.animate) return;

        boolean allSame = true;

        for(int i = 0; i < channels.length; i++)
        {
            if(!getAnim(i).equals(m.currentAnims[i]))
            {
                allSame = false;
                break;
            }
        }

        if(allSame) return;

        for(int i = 0; i < ThirdPersonCharacterControl.bodyNodes.length; i++)
        {
            channels[i].setAnim(nameMap.get(m.currentAnims[i]));
        }
    }

    /**
     * Gets the currently running animation for an animation channel.
     * @param i The index of the animation channel.
     * @return Its current animation.
     */
    private String getAnim(int i)
    {
        // Gets the name (as registered in the model) of the current animation
        String anim = channels[i].getAnimationName();
        // If the animation is null, return a blank string.
        // This is to avoid NullPointerExceptions
        if (anim == null)
        {
            return "";
        }
        // Creates a variable to store the corresponding key (the name of the animation in our naming system)
        String animKey = anim;
        // For each possible animation key
        for (String key : nameMap.keySet())
        {
            // If its corresponding animation matches the one that is currently running
            if (anim.equals(nameMap.get(key)))
            {
                // Set that as the correct key; break out of the for loop
                animKey = key;
                break;
            }
        }
        // Return the animation key provided to you
        return animKey;
    }
}

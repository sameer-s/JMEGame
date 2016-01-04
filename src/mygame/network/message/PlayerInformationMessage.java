package mygame.network.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * A message that communicates the information stored about a player over the network.
 * @author Sameer Suri
 */
// Allows the SpiderMonkey (jMonkeyEngine network library) serializer to serialize this object.
@Serializable()
public class PlayerInformationMessage extends AbstractMessage
{
    /**
     * Positional information about the location of the player.
     * Should have 3 members:
     * index 0 -> x, index 1 -> y, index 2 -> z
     */
    public float[] location;

    /**
     * Positional information about the rotation of the player.
     * Should have 4 members:
     * index 0 -> x, index 1 -> y, index 2 -> z, index 3 -> w
     * There are 4 values because these are quaternion values, not euler angles.
     */
    public float[] rotation;

    /**
     * Information about the current animation (for each anim).
       This is the name (map key) defined in the 3rd person character control.
       This is NOT the name of the animation as defined in the 3D model.
       THIS MUST BE DEFINED IN THE SAME ORDER AS THE ARRAY:
            mygame.character.ThirdPersonCharacterControl.bodyNodes;
     */
    public String[] currentAnims;

    // TODO: pass LoopMode, possibly as ordinal ints.

    /**
     * An empty constructor. Required for the SpiderMonkey serializer.
     */
    public PlayerInformationMessage(){};
}

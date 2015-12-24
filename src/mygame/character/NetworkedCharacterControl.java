/*
 * Copyright (C) 2015 Sameer Suri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mygame.character;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import mygame.network.message.PlayerInformationMessage;

/**
 * A class to control the 'other' third person character, over the network.
 * Although some aspects of this class are specific to this game, I have
 * tried to create it so that it would not be difficult to port to a
 * different game.
 * @author Sameer Suri
 */
public class NetworkedCharacterControl extends RigidBodyControl
{
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

    /**
     * Constructor for the control.
     * @param animations The map of animations, so the control knows which ones to use
     * @param spatial The model used, so that the control can find the animations in the model
     */
    public NetworkedCharacterControl(HashMap<String, String> animations, Spatial spatial)
    {
        // Calls the RigidBodyControl constructor.
        super(ThirdPersonCharacterControl._mass);

//        setKinematic(true);

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

        // Sets the current animation to the idle animation
        setAnim("Idle");
    }

    public void updateFromMessage(PlayerInformationMessage pim)
    {
        setPhysicsLocation(new Vector3f(pim.location[0], pim.location[1], pim.location[2]));
        setPhysicsRotation(new Quaternion(pim.rotation[0], pim.rotation[1], pim.rotation[2], pim.rotation[3]));

        boolean allSame = true;
        for(int i = 0; i < bodyNodes.length; i++)
        {
            if(!getAnim(bodyNodes[i]).equals(pim.currentAnims[i]))
            {
                allSame = false;
                break;
            }
        }

        if(allSame) return;

        for(int i = 0; i < bodyNodes.length; i++)
        {
            setAnim(pim.currentAnims[i], bodyNodes[i]);
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
        // Not guaranteed to be accurate for them all
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
}

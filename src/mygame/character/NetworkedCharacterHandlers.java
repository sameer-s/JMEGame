package mygame.character;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import static mygame.character.ThirdPersonCharacterControl.moveSpeed;

/**
 * Handlers for the other player's character.
 * One takes care of animation, the other does movement.
 * Rotation is done via 'teleporting' and has no handler.
 * @author Sameer Suri
 */
public class NetworkedCharacterHandlers
{
    /**
     * Handles the movement for the other player's character.
     * The target locations are transferred over network.
     * Since this only needs to be used once, everything is static.
     * @author Sameer Suri
     */
    public static class MovementHandler
    {
        public static Vector3f move(Vector3f location, Vector3f target, float tpf)
        {
            final float distance = location.distance(target);
            if(distance == 0)
                return location;

            final Vector3f movement = target.subtract(location);

            Vector3f adjusted = movement.mult((tpf * moveSpeed) / distance);

            return adjusted.length() > movement.length() ? movement.add(location) : adjusted.add(location);
        }
    }
    /**
     * Handles the animations for the other player's character.
     * These animations are transferred over network.
     * Since this only needs to be used once, everything is static.
     * @author Sameer Suri
     */
    public static class AnimationHandler
    {
        /**
         * The animation channels of the opposing player's model
         */
        private static AnimChannel[] channels;
        /**
         * An object to map names used by this game to names used by this model.
         */
        private static HashMap<String, String> nameMap;

        /**
         * Initializes the variables for the animation handler.
         * @param spatial The animation to use
         * @param nameMap
         */
        public static void init(Spatial spatial, HashMap<String, String> nameMap)
        {
            // The body nodes can be found at: mygame.character.ThirdPersonCharacterControl.bodyNodes
            // Gives enough space for each of the body nodes.
            channels = new AnimChannel[ThirdPersonCharacterControl.bodyNodes.length];
            // Loops over each body node
            for (int i = 0; i < ThirdPersonCharacterControl.bodyNodes.length; i++)
            {
                // Gets the corresponding spatial
                Spatial bodyPart = ((Node) spatial).getChild(ThirdPersonCharacterControl.bodyNodes[i]);
                // Gets the animation control
                AnimControl control = bodyPart.getControl(AnimControl.class);
                // Creates a channel for that control and places it in our array
                channels[i] = control.createChannel();
            }
            // Sets the map to the parameter map.
            AnimationHandler.nameMap = nameMap;
        }

        /**
         * Updates the spatial model passed in init with a set of animations.
         * @param animNames The animations to apply. Must be in the same order as the bodyNodes.
         */
        public static void updateAnims(String[] animNames)
        {
            // If all the animations are the same, we don't want to update them.
            // That would cause the animations to get stuck on the first frame, because it would keep resetting.
            // Tracks if the animations are all the same
            boolean allSame = true;
            // Loops over each of our animation channel
            for (int i = 0; i < channels.length; i++)
            {
                // If the current animation is different from the new one
                if (!getAnim(i).equals(animNames[i]))
                {
                    // They are not all the same; update the flag, and exit, as there is no point to keep searching.
                    allSame = false;
                    break;
                }
            }

            // If they're all the same, return. We don't need to update the animations.
            if (allSame)
            {
                return;
            }
            // Otherwise, update the corresponding animation for each channel.
            for (int i = 0; i < ThirdPersonCharacterControl.bodyNodes.length; i++)
            {
                channels[i].setAnim(nameMap.get(animNames[i]));
            }
        }

        /**
         * Gets the currently running animation for an animation channel.
         * @param i The index of the animation channel.
         * @return Its current animation.
         */
        private static String getAnim(int i)
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

}

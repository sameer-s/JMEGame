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
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import static mygame.character.ThirdPersonCharacterControl.bodyNodes;

/**
 *
 * @author Sameer Suri
 */
public class NetworkedCharacterAnimationHandler
{
    private static AnimChannel[] channels;
    private static HashMap<String, String> nameMap;

    public static void init(Spatial spatial, HashMap<String, String> nameMap)
    {
        channels = new AnimChannel[bodyNodes.length];

        for(int i = 0; i < bodyNodes.length; i++)
        {
            Spatial bodyPart = ((Node) spatial).getChild(bodyNodes[i]);

            AnimControl control = bodyPart.getControl(AnimControl.class);

            channels[i] = control.createChannel();
        }

        NetworkedCharacterAnimationHandler.nameMap = nameMap;
    }
    public static void updateAnims(String[] animNames)
    {
        boolean allSame = true;
        for(int i = 0; i < channels.length; i++)
        {
            if(!getAnim(i).equals(animNames[i]))
            {
                allSame = false;
                break;
            }
        }

        System.out.printf("All same = %s\n", "" + allSame);

        if(allSame) return;

        for(int i = 0; i < bodyNodes.length; i++)
        {
            channels[i].setAnim(nameMap.get(animNames[i]));
        }
    }

    private static String getAnim(int i)
    {
        // Gets the name (as registered in the model) of the current animation
        String anim = channels[i].getAnimationName();

        if(anim == null)
            return "";

        // Creates a variable to store the corresponding key (the name of the animation in our naming system)
        String animKey = anim;

        // For each possible animation key
        for(String key : nameMap.keySet())
        {
            // If its corresponding animation matches the one that is currently running
            if(anim.equals(nameMap.get(key)))
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

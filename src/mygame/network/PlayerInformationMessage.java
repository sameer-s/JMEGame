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
package mygame.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * A message that communicates the information stored about a player over the network.
 * @author Sameer Suri
 */
// Allows the SpiderMonkey (jMonkeyEngine network library) serializer to
// serialize this object.
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
     * Positional information about the rotation of the player
     * Should have 4 members:
     * index 0 -> w, index 1 -> x, index 2 -> y, index 3 -> z
     * There are 4 values because these are quaternion values, not eulers.
     */
    public float[] rotation;

    /**
     * Information about the current animation (for each anim).
       This is the name (map key) defined in the 3rd person character control.
       This is NOT the name of the animation as defined in the 3D model.
       THIS MUST BE DEFINED IN THE SAME ORDER AS THE ARRAY:
            ThirdPersonCharacterControl.bodyNodes;
     */
    public String[] currentAnims;

    /**
     * An empty constructor. Required for the SpiderMonkey serializer.
     */
    public PlayerInformationMessage(){};
}

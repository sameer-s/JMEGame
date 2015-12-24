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
package mygame.network.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Sameer Suri
 */
@Serializable
public class PlayerConnectionMessage extends AbstractMessage
{
    // true -> connect; false -> disconnect
    public boolean isConnection;

    public PlayerConnectionMessage setIsConnection(boolean isConnection)
    {
        this.isConnection = isConnection;
        return this;
    }

    /**
     * An empty constructor. Required for the SpiderMonkey serializer.
     */
    public PlayerConnectionMessage(){};
}

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

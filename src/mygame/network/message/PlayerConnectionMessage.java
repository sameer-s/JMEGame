package mygame.network.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Informs a client when another client has connected or disconnected.
 * Sent by the server.
 * @author Sameer Suri
 */
// Allows the SpiderMonkey (jMonkeyEngine network library) serializer to serialize this object.
@Serializable
public class PlayerConnectionMessage extends AbstractMessage
{
    // Whether the message indicates a connection or disconnection.
    // True would mean a connection, false would correspond to a disconnection.
    public boolean isConnection;

    // Sets the boolean and returns this to allow chaining
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

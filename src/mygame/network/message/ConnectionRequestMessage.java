package mygame.network.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Sameer Suri
 */
@Serializable
public class ConnectionRequestMessage extends AbstractMessage
{
    // Assigned by the server.
    public ConnectionStatus status;

    public static enum ConnectionStatus
    {
        PLAYER1, PLAYER2, REQUEST_DENIED;
    }

    /**
     * An empty constructor. Required for the SpiderMonkey serializer.
     */
    public ConnectionRequestMessage(){};
}

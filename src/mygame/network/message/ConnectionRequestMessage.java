package mygame.network.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * A message for a client to request connection to a server.
 * It is a type of 'handshake', first the client sends their connection request.
 * Then the server responds.
 * The server will also send a status.
 * It will either tell the client which player they are, or that their request was denied.
 * The request denied message would be sent if there are already 2 players on the server.
 * @author Sameer Suri
 */
// Allows the SpiderMonkey (jMonkeyEngine network library) serializer to serialize this object.
@Serializable
public class ConnectionRequestMessage extends AbstractMessage
{
    // Assigned by the server.
    public ConnectionStatus status;

    // The possible states
    public static enum ConnectionStatus
    {
        PLAYER1, PLAYER2, REQUEST_DENIED;
    }

    /**
     * An empty constructor. Required for the SpiderMonkey serializer.
     */
    public ConnectionRequestMessage(){};
}

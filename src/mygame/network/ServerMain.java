package mygame.network;

import com.jme3.app.SimpleApplication;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.net.Inet4Address;
import mygame.network.message.ConnectionRequestMessage;
import mygame.network.message.ConnectionRequestMessage.ConnectionStatus;
import mygame.network.message.PlayerConnectionMessage;
import mygame.network.message.PlayerInformationMessage;

/**
 * The server that takes care of networking.
 * Mostly, it sends messages between clients.
 * @author Sameer Suri
 */
public class ServerMain extends SimpleApplication implements ConnectionListener, MessageListener<HostedConnection>
{
    /**
     * The port to host the server on.
     */
    public static final int PORT = 6143;

    // The reference to the actual server
    private Server server;

    // The connection to the two actual players
    private HostedConnection player1, player2;

    // Starting point for the server app
    public static void main(String[] args)
    {
        // Starts the server in headless mode (no window, etc.)
        ServerMain app = new ServerMain();
        app.start(JmeContext.Type.Headless);
    }

    // Called when the server is started in headless mode
    @Override
    public void simpleInitApp()
    {
        // Registers our custom message type with the networking system
        Serializer.registerClasses(PlayerInformationMessage.class,
                           ConnectionRequestMessage.class,
                           PlayerConnectionMessage.class);

        try
        {
            // Creates the server and starts it.
            server = Network.createServer(PORT);
            server.start();

            // Adds the connection listener so we can be notified when a client connects or leaves
            server.addConnectionListener(this);
            // Allows this to recieve messages from clients
            server.addMessageListener(this);
            
            System.out.printf("Server hosted:%n\tIP=\"%s\"%n\tPort=\"%d\"", Inet4Address.getLocalHost().getHostAddress(), PORT);
        }
        catch(IOException e)
        {
            // If an error occured, print it to the console, and exit.
            System.err.println("UNABLE TO START SERVER ON PORT: " + PORT);
            System.err.println("STACK TRACE:");
            e.printStackTrace(System.err);
            stop();
            // Kill the JVM
            System.exit(0);
        }
    }

    // Called by the engine to clean up resources
    @Override
    public void destroy()
    {
        // Stops the networking to end the connection cleanly (kills the server)
        if(server != null)
            server.close();

        // Has the superclass finish cleanup
        super.destroy();
    }

    // These methods notify the server when a client connects or disconnects

    // Called when a client connects
    @Override
    public void connectionAdded(Server server, HostedConnection conn)
    {
        // Empty, as we have no use for this information.
        // We don't acknowledge a player as actually having been added to the game until:
        // A.) They send a handshake message
        // B.) The server gives them a status that is not REQUEST_DENIED
    }

    // Called when a client disconnect1
    @Override
    public void connectionRemoved(Server server, HostedConnection conn)
    {
        // If player 1 is the player who disconnected
        if(conn.equals(player1))
        {
            // Update this in the corresponding variable
            player1 = null;
            //Log it
            System.out.println("Player 1 disconnected. ID = " + conn.getId());

            // Notify Player 2
            if(player2 != null)
            {
                // Log it
                System.out.println("Informing player 2...");
                // Tell player 1 of a disconnection, which uses reliable (TCP) communication
                player2.send(new PlayerConnectionMessage().setIsConnection(false).setReliable(true));
            }
        }
        // If player 2 is the player who disconnected
        else if (conn.equals(player2))
        {
            // Update this in the corresponding variable
            player2 = null;
            // Log it
            System.out.println("Player 2 disconnected. ID = " + conn.getId());

            // Notify Player 1
            if(player1 != null)
            {
                // Log it
                System.out.println("Informing player 1...");
                // Tell player 1 of a disconnection, which uses reliable (TCP) communication
                player1.send(new PlayerConnectionMessage().setIsConnection(false).setReliable(true));
            }
        }
    }

    // When a client sends a message
    @Override
    public void messageReceived(HostedConnection source, Message m)
    {
        // If a client is requesting a connection
        if(m instanceof ConnectionRequestMessage)
        {
            ConnectionRequestMessage crm = new ConnectionRequestMessage();
            crm.setReliable(true);

            // If there is no player 1
            if(player1 == null)
            {
                // They have become player 1.
                // Update the variable
                player1 = source;
                // Update the message
                crm.status = ConnectionStatus.PLAYER1;
                // Log it
                System.out.println("Player 1 connected. ID = " + source.getId());

                // If there is a player 2
                if(player2 != null)
                {
                    // Tell them using reliable (TCP) communication
                    player2.send(new PlayerConnectionMessage().setIsConnection(true).setReliable(true));
                }
            }
            // Otherwise, if there is no player 2
            else if (player2 == null)
            {
                // They have become player 2
                // Update the variable
                player2 = source;
                // Update the message
                crm.status = ConnectionStatus.PLAYER2;
                // Log it
                System.out.println("Player 2 connected. ID = " + source.getId());

                // If there is a player 1
                if(player1 != null)
                {
                    // Tell them using reliable (TCP) communication
                    player1.send(new PlayerConnectionMessage().setIsConnection(true).setReliable(true));
                }
            }
            // Otherwise
            else
            {
                // The server is full, update the message accordingly
                crm.status = ConnectionStatus.REQUEST_DENIED;
            }

            // Log the connection status being assigned
            System.out.println("Assigning connection status: " + crm.status.toString());
            // Reply to the person who requested connection, informing them of their status
            source.send(crm);
        }
        // If someone has sent info about their player's location
        else if(m instanceof PlayerInformationMessage)
        {
            // We want to pass it on to the other player

            // If either player is null, this is impossible. Return.
            if(player1 == null || player2 == null) return;

            // If the source is player 1...
            if(source.equals(player1))
            {
                // This message came from player 1, pass it on to player 2
                player2.send(m);
            }
            // If the source is player 2...
            else if(source.equals(player2))
            {
                // This message came from player 2, pass it on to player 1
                player1.send(m);
            }
        }
    }
}

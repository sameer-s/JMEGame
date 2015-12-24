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
import mygame.network.message.ConnectionRequestMessage;
import mygame.network.message.ConnectionRequestMessage.ConnectionStatus;
import mygame.network.message.PlayerConnectionMessage;
import mygame.network.message.PlayerInformationMessage;

/**
 * The server that takes care of networking.
 * @author Sameer Suri
 */
public class ServerMain extends SimpleApplication implements ConnectionListener, MessageListener<HostedConnection>
{
    /**
     * The port to host the server on.
     */
    public static final int PORT = 6143;

    private Server server;

    private HostedConnection player1, player2;

    public static void main(String[] args)
    {
        // Starts the server in headless mode (no window, etc.)
        ServerMain app = new ServerMain();
        app.start(JmeContext.Type.Headless);
    }

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
        }
        catch(IOException e)
        {
            System.err.println("UNABLE TO START SERVER ON PORT: " + PORT);
            System.err.println("STACK TRACE:");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Called in a loop by the engine to allow the server to make changes.
     * @param tpf "Time per frame"; the amount of time taken between the last update cycle and this one
     */
    @Override
    public void simpleUpdate(float tpf)
    {
    }

    /**
     * Cleans up resources and kills the server.
     */
    @Override
    public void destroy()
    {
        // Stops the networking to end the connection cleanly
        if(server != null)
            server.close();

        // Has the superclass finish cleanup
        super.destroy();
    }

    // These methods notify the server when a client connects or disconnects
    @Override
    public void connectionAdded(Server server, HostedConnection conn)
    {
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn)
    {
        if(conn.equals(player1))
        {
            player1 = null;
            System.out.println("Player 1 disconnected. ID = " + conn.getId());

            if(player2 != null)
            {
                System.out.println("Informing player 2...");
                player2.send(new PlayerConnectionMessage().setIsConnection(false).setReliable(true));
            }
        }
        else if (conn.equals(player2))
        {
            player2 = null;
            System.out.println("Player 2 disconnected. ID = " + conn.getId());

            if(player1 != null)
            {
                System.out.println("Informing player 1...");
                player1.send(new PlayerConnectionMessage().setIsConnection(false).setReliable(true));
            }
        }
    }

    // When a client sends a message

    @Override
    public void messageReceived(HostedConnection source, Message m)
    {
        if(m instanceof ConnectionRequestMessage)
        {
            ConnectionRequestMessage crm = new ConnectionRequestMessage();
            crm.setReliable(true);

            if(player1 == null)
            {
                player1 = source;
                crm.status = ConnectionStatus.PLAYER1;
                System.out.println("Player 1 connected. ID = " + source.getId());

                if(player2 != null)
                {
                    player2.send(new PlayerConnectionMessage().setIsConnection(true).setReliable(true));
                }
            }
            else if (player2 == null)
            {
                player2 = source;
                crm.status = ConnectionStatus.PLAYER2;
                System.out.println("Player 2 connected. ID = " + source.getId());

                if(player1 != null)
                {
                    player1.send(new PlayerConnectionMessage().setIsConnection(true).setReliable(true));
                }
            }
            else
            {
                crm.status = ConnectionStatus.REQUEST_DENIED;
            }

            System.out.println("Assigning connection status: " + crm.status.toString());
            source.send(crm);
        }
        else if(m instanceof PlayerInformationMessage)
        {
            if(player1 == null || player2 == null) return;

            if(source.equals(player1))
            {
                // This message came from player 1, pass it on to player 2
                player2.send(m);
            }
            else if(source.equals(player2))
            {
                // This message came from player 2, pass it on to player 1
                player1.send(m);
            }
        }
    }
}

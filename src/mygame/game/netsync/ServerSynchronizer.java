package mygame.game.netsync;

import com.jme3.network.Server;
import com.jme3.scene.Node;
/**
 *
 * @author Sameer Suri
 */
public class ServerSynchronizer
{
    private Node rootNode;

    private Server server;

    public ServerSynchronizer(Server server)
    {
        this.server = server;
        NetSyncMessage.setup();
    }
    
    public ServerSynchronizer(Server server, Node rootNode)
    {
        this(server);
        this.rootNode = rootNode;
    }

    public ServerSynchronizer setRootNode(Node rootNode)
    {
        this.rootNode = rootNode;
        return this;
    }

    public void update()
    {
        server.broadcast(new NetSyncMessage.Root().setRootNode(rootNode));
    }

    public void closeServer()
    {
        if(server != null && server.isRunning())
        {
            server.close();
        }
    }
}

package mygame.game.netsync;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import com.jme3.scene.Spatial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sameer Suri
 */
public class ServerSynchronizer implements ConnectionListener
{
    private Set<Spatial> spatials = new HashSet<>();

    private Server server;


    public ServerSynchronizer(Server server)
    {
        this.server = server;
        NetSyncMessage.setup();
        server.addConnectionListener(this);
    }

    public void trackSpatials(Spatial... spatials)
    {
        for(Spatial spatial : spatials)
        {
            this.spatials.add(spatial);

            server.broadcast(new NetSyncMessage.New().setSpatial(spatial));
        }
    }

    public void trackSpatials(Collection<Spatial> spatials)
    {
        trackSpatials(spatials.toArray(new Spatial[spatials.size()]));
    }

    public void untrackSpatials(Spatial... spatials)
    {
        for(Spatial spatial : spatials)
        {
            this.spatials.remove(spatial);

            server.broadcast(new NetSyncMessage.Removed().setName(spatial.getName()));
        }
    }

    public void untrackSpatials(Collection<Spatial> spatials)
    {
        untrackSpatials(spatials.toArray(new Spatial[spatials.size()]));
    }

    public void update()
    {
        for(Spatial spatial : spatials)
        {
            server.broadcast(NetSyncMessage.Update.from(spatial));
        }
    }

    public void closeServer()
    {
        if(server != null && server.isRunning())
        {
            server.close();
        }
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn)
    {
        for(Spatial spatial : spatials)
        {
            conn.send(new NetSyncMessage.New().setSpatial(spatial));
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn)
    {
        // who cares?
    }
}

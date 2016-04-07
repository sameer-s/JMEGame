package mygame.game.netsync;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssuri4121
 */
public class ClientSynchronizer implements MessageListener<Client>
{
    private List<SynchronizationCallback> callbacks = new ArrayList<>();
    
    public ClientSynchronizer addCallback(SynchronizationCallback callback)
    {
        callbacks.add(callback);
        return this;
    }
    
    public ClientSynchronizer removeCallback(SynchronizationCallback callback)
    {
        callbacks.remove(callback);
        return this;
    }
    
    @Override
    public void messageReceived(Client source, Message m)
    {
        
    }
    
    public static interface SynchronizationCallback
    {
        public void synchronize(Node rootNode);
    }
}

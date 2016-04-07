package mygame.game.netsync;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Sameer Suri
 */
public class ClientSynchronizerNoRoot implements MessageListener<Client>
{
    private List<Spatial> spatials = new ArrayList<>();

    private static final Comparator<Spatial> COMPARATOR_NAME = (spatial1, spatial2) ->
    {
        return spatial1.getName().compareTo(spatial2.getName());
    };

    public ClientSynchronizerNoRoot(Client client)
    {
        NetSyncMessage.setup();
        client.addMessageListener(this);
    }

    @Override
    public void messageReceived(Client source, Message m)
    {
        if(m instanceof NetSyncMessage.New)
        {
            NetSyncMessage.New newMessage = (NetSyncMessage.New) m;
            int index = Collections.binarySearch(spatials, newMessage.spatial, COMPARATOR_NAME);

            if(index < 0)
            {
                spatials.add(newMessage.spatial);
                spatials.sort(COMPARATOR_NAME);
            }
            else
            {
                spatials.set(index, newMessage.spatial);
            }
        }
        else if(m instanceof NetSyncMessage.Update)
        {
            NetSyncMessage.Update updateMessage = (NetSyncMessage.Update) m;
            Geometry geom = new Geometry();
            geom.setName(updateMessage.name);
            int index = Collections.binarySearch(spatials, geom, COMPARATOR_NAME);

            if(index >= 0)
            {
                Spatial spatial = spatials.get(index);

                spatial.setLocalTranslation(updateMessage.location);
                spatial.setLocalRotation(updateMessage.rotation);
                spatial.setLocalScale(updateMessage.scale);
            }
        }
        else if(m instanceof NetSyncMessage.Removed)
        {
            NetSyncMessage.Removed removedMessage = (NetSyncMessage.Removed) m;
            Geometry geom = new Geometry();
            geom.setName(removedMessage.name);
            int index = Collections.binarySearch(spatials, geom, COMPARATOR_NAME);

            spatials.remove(index);
        }
    }
}

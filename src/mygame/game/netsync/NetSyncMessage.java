package mygame.game.netsync;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Spatial;

/**
 *
 * @author Sameer Suri
 */
public abstract class NetSyncMessage
{
    // do not invoke!
    // automatically called by synchronizers
    static void setup()
    {
        Serializer.registerClasses(New.class, Update.class, Removed.class);
    }

    @Serializable
    public static class New extends AbstractMessage
    {
        public Spatial spatial;

        public New setSpatial(Spatial spatial)
        {
            this.spatial = spatial;
            return this;
        }

        public New()
        {
            setReliable(true);
        }
    }

    // The Updating message class ONLY tracks location, rotation and scale, for size reasons
    // If you change anything else, i.e. the mesh, you MUST call ServerSynchronizer::trackSpatial(Spatial) again
    @Serializable
    public static class Update extends AbstractMessage
    {
        public String name;
        public Vector3f location;
        public Quaternion rotation;
        public Vector3f scale;

        public static Update from(Spatial spatial)
        {
            Update update = new Update();
            update.name = spatial.getName();
            update.location = spatial.getLocalTranslation();
            update.rotation = spatial.getLocalRotation();
            update.scale = spatial.getLocalScale();

            return update;
        }

        public Update setName(String name)
        {
            this.name = name;
            return this;
        }

        public Update setLocation(Vector3f location)
        {
            this.location = location;
            return this;
        }

        public Update setRotation(Quaternion rotation)
        {
            this.rotation = rotation;
            return this;
        }

        public Update setScale(Vector3f scale)
        {
            this.scale = scale;
            return this;
        }

        public Update()
        {
            setReliable(false);
        }
    }

    @Serializable
    public static class Removed extends AbstractMessage
    {
        public String name;

        public Removed setName(String name)
        {
            this.name = name;
            return this;
        }

        public Removed()
        {
            setReliable(true);
        }
    }
}

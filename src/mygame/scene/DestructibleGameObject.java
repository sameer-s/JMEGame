package mygame.scene;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sameer Suri
 */
public interface DestructibleGameObject
{
    public void destroyGameObject(Map<String, Object> data);

    public default boolean shouldBeDestroyed(Map<String, Object> data)
    {
        return true;
    }

    public default Map<String, Object> getInfo()
    {
        return new HashMap<>();
    }
}

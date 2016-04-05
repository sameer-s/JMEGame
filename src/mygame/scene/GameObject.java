package mygame.scene;

import java.util.ArrayList;

/**
 *
 * @author Sameer Suri
 */
public abstract class GameObject
{
    public static interface Destructible
    {
        public void destroyGameObject();
    }

    public static interface Taggable
    {
        public ArrayList<String> getTags();

        public static ArrayList<String> getTags(Object o)
        {
            if(o instanceof Taggable)
            {
                return ((Taggable) o).getTags();
            }
            else
            {
                return new ArrayList<>();
            }
        }
    }
}

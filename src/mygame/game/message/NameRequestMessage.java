package mygame.game.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Sameer Suri
 */
@Serializable()
public class NameRequestMessage extends AbstractMessage
{
    public String name = "";

    public NameRequestMessage()
    {
        setReliable(true);
    }
}

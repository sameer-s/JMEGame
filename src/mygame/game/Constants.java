package mygame.game;

import mygame.game.message.InputEventMessage;
import mygame.game.message.NameRequestMessage;

/**
 *
 * @author Sameer Suri
 */
public abstract class Constants
{
    public static abstract class Scene
    {
        public static final String SHIP_MODEL = "Models/ship/SpaceShip.j3o";
    }

    public static abstract class Network
    {
        public static final int PORT = 6143;
        public static final Class[] messageClasses = new Class[]{NameRequestMessage.class, InputEventMessage.Action.class, InputEventMessage.Analog.class};
    }
}

package mygame.game.message;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Sameer Suri
 */
public abstract class InputEventMessage
{
    @Serializable()
    public static class Action extends AbstractMessage
    {
        public String action = "";
        public boolean isPressed = false;
        public float tpf = 0f;

        public Action(String action, boolean isPressed, float tpf)
        {
            this.action = action;
            this.isPressed = isPressed;
            this.tpf = tpf;
        }

        public Action() {}
    }

    @Serializable()
    public static class Analog extends AbstractMessage
    {
        public String action = "";
        public float value = 0f;
        public float tpf = 0f;

        public Analog(String action, float value, float tpf)
        {
            this.action = action;
            this.value = value;
            this.tpf = tpf;
        }

        public Analog() {}
    }
}

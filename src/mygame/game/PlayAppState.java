package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.controls.ActionListener;

/**
 * The app state where the player is interacting with the world and other player.
 * This is the main game, and all of the actual game code is here.
 * @author Sameer Suri
 */
public class PlayAppState extends AbstractAppState implements ActionListener
{
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
    }
}

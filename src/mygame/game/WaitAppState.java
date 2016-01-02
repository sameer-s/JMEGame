package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

/**
 *
 * @author Sameer Suri
 */
public class WaitAppState extends AbstractAppState
{
    // Holds a reference to the app
    private Main app;

    // The Spatial representing the text that shows up - "Waiting..."
    private BitmapText text;

    // Called when the app state first starts
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        // Has the superclass take care of some initialization
        super.initialize(stateManager, app);

        // Stores the app in a variable for later use
        this.app = (Main) app;
        // Creates a new text node.
         // default font, NOT right to left
        text = new BitmapText(this.app.getGuiFont(), false);
        // Default size
        text.setSize(this.app.getGuiFont().getCharSet().getRenderedSize());
        // Orange color: RGB=(251/255, 130/255, 0)
        text.setColor(ColorRGBA.Orange);
        // Sets it to have that text
        text.setText("Waiting for one more player to join...");
        // Puts it in roughly the middle
        text.setLocalTranslation(this.app.getSettings().getWidth() / 2, this.app.getSettings().getHeight() / 2, 0);
        // Adds it to the world in an orthographic view (GUI node)
        this.app.getGuiNode().attachChild(text);

        this.app.clientMessageListener.setAppState(this); // Sets the current app state to be this one
    }

    /**
     * Returns true if the player is Player 1 and false if it is Player 2
     * @return If the player is player 1, true, if not, false
     */
    public boolean isPlayer1()
    {
        return app.isPlayer1;
    }

    // Called by the client message listener to inform this app state that we are done waiting
    public void finish()
    {
        app.nextAppState();
    }

    // Called by the engine when this app state is being destroyed
    @Override
    public void cleanup()
    {
        // Has the engine take care of the default cleanup
        super.cleanup();

        // Removes the text, we don't need it nor want it getting in the way
        app.getGuiNode().detachChild(text);
    }

}

package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import mygame.Main;

/**
 *
 * @author Sameer Suri
 */
public class WaitingAppState extends AbstractAppState
{
    private Main app;
    private BitmapText text;

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        this.app = (Main) app;
        text = new BitmapText(this.app.getGuiFont(), false);
        text.setSize(this.app.getGuiFont().getCharSet().getRenderedSize());
        text.setColor(ColorRGBA.Orange);
        text.setText("Waiting for one more player to join...");
        text.setLocalTranslation(this.app.getSettings().getWidth() / 2, this.app.getSettings().getHeight() / 2, 0);
        this.app.getGuiNode().attachChild(text);

        this.app.clientMessageListener.setAppState(this);
    }

    public boolean isPlayer1()
    {
        return app.isPlayer1;
    }

    public void finish()
    {
        app.nextAppState();
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        app.getGuiNode().detachChild(text);
    }

}

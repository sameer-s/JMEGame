package mygame.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.font.BitmapFont;
import com.jme3.network.Client;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import mygame.character.NetworkedCharacterControl;
import mygame.character.ThirdPersonCharacterControl;
import mygame.debug.DebugLogger;
import mygame.network.ClientMessageListener;

/**
 * The main class for the client application. It has a number of purposes:
 * This is the starting point for the game.
 * Handles the app states that take care of the game
 * Handles the player controller and other player spatial for the characters.
 *
 * The AppStates can be found in the package: mygame.game (where this file is)
 *
 * @author Sameer Suri
 */
public class Main extends SimpleApplication
{
    /* Game stuff: */

    // The player controller for the player controlled by THIS client.
    ThirdPersonCharacterControl playerController;

    NetworkedCharacterControl otherPlayerController;

    // The networking client.
    Client client;

    // A listener for messages sent by the server.
    ClientMessageListener clientMessageListener;

    // A flag. true if this player is player 1, false otherwise.
    boolean isPlayer1;

    // Tracks the current state of the app.
    // Three states currently exist, Init, Wait and Play.
    private AppState currentAppState;

    // Starting point of the client app
    public static void main(String... args)
    {
        // Creates a new instance of our game and starts it.
        Main gp = new Main();
        gp.start();
    }

    public Main()
    {
        super(new AppState[] {});
    }

    // Called by the engine in order to start the game.
    @Override
    public void simpleInitApp()
    {
        settings.setUseJoysticks(true);

        this.setPauseOnLostFocus(false);

        inputManager.setCursorVisible(false);


        /*
            This game is implemented through App States.
            The first app state is Init.
            Then, if the server has two players, it will go to the Play app state.
            If not, it will go to the Wait app phase until a second player joins.
            Then it will enter Play app state.
            Once it enters the Play app state, it will play the game.
            A 'menu' app state will also be added at some point.
            For info about each app state, read the comments in its source file.
        */

        // Starts off with the Init app state.
        currentAppState = new InitAppState();
        stateManager.attach(currentAppState);
    }

    // Cleans up resources, closes the window, and kills the app.
    @Override
    public void destroy()
    {
        // Stops the networking to end the connection cleanly
        if(client != null)
            client.close();

        DebugLogger.close();

        // Has the superclass finish cleanup
        super.destroy();
    }

    // Called by an app state when it is finished.
    void nextAppState()
    {
        // This is a quick diagram of app states:
        // Init -> (Wait) -> Play <-> Menu
        // Menu does not currently exist

        // Removes the already attached state so a new one can be added
        stateManager.detach(currentAppState);

        // A boolean to track if we are actually adding a new app state, to avoid
        // a NullPointerException
        boolean newStateAttached = true;

        // Goes to either Wait or Play after Init
        if(currentAppState instanceof InitAppState)
        {
            // Goes to Wait if we are player 1 (and thus, we need a player 2)
            if(this.isPlayer1)
            {
                currentAppState = new WaitAppState();
            }
            // Goes to Play if we are player 2 (and thus, a player 1 has already connected)
            else
            {
                currentAppState = new PlayAppState();
            }
        }
        // Goes to Play after Wait
        else if(currentAppState instanceof WaitAppState)
        {
            currentAppState = new PlayAppState();
        }
        // If no app state was changed, reflect that in the boolean flag
        else
        {
            newStateAttached = false;
        }

        if(newStateAttached)
        {
            // Actually attaches the next app state
            stateManager.attach(currentAppState);
        }
    }

    /**
     * Gets the default font for use with the GUI
     * @return The default GUI font
     */
    public BitmapFont getGuiFont()
    {
        return guiFont;
    }

    /**
     * Gets the settings established by the player in the settings menu.
     * @return The settings.
     */
    public AppSettings getSettings()
    {
        return settings;
    }

    public void addSpatial(Spatial sp, PhysicsControl... pcs)
    {
        rootNode.attachChild(sp);
        for(PhysicsControl pc : pcs)
        {
            sp.addControl(pc);
            this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(pc);
        }
    }
}

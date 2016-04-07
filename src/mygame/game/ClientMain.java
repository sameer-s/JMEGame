package mygame.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.util.UUID;
import javax.swing.JOptionPane;
import mygame.debug.DebugLogger;
import mygame.game.message.InputEventMessage;
import mygame.game.message.NameRequestMessage;
import org.lwjgl.input.Keyboard;

/**
 * The main class for the client application. It has a number of purposes:
 * This is the starting point for the game.
 * Handles the app states that take care of the game
 * Handles the player controller and other player spatial for the characters.
 *
 * @author Sameer Suri
 */
public class ClientMain extends SimpleApplication implements MessageListener<Client>, ActionListener, AnalogListener
{
    /* Game stuff: */

    public static ClientMain instance;

    // The networking client.
    Client client;

    // Tracks the current state of the app.
    private AppState currentAppState;

    public static void main(String... args)
    {
        ClientMain gp = new ClientMain();
        instance = gp;
        gp.start();
    }

    public ClientMain()
    {
        super(new AppState[] {});
    }

    @Override
    public void simpleInitApp()
    {
        settings.setUseJoysticks(true);

        this.setPauseOnLostFocus(false);

        initKeys();
        inputManager.setCursorVisible(false);

        Serializer.registerClasses(Constants.Network.messageClasses);

        String address = (String) JOptionPane.showInputDialog(null,
             "Enter the address of the server.\nBlank assumes 'localhost'.",
            "Server address?",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            null);
        // If the address provided is null (no answer given), assume 'localhost'
        address = address == null ? "localhost" : address;

        try
        {
            client = Network.connectToServer(address, Constants.Network.PORT);
            client.start();

            client.addMessageListener(this);
        }
        catch(IOException e)
        {
            System.err.println("UNABLE TO CONNECT TO SERVER ON PORT: " + Constants.Network.PORT);
            System.err.println("STACK TRACE:");
            e.printStackTrace(System.err);

            stop();

            JOptionPane.showMessageDialog(
                null,
                "Unable to connect to the server.\nReason: " + e.getLocalizedMessage(),
                "Error: " + e.getClass().getName(),
                JOptionPane.ERROR_MESSAGE);

            System.exit(0);
        }
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

        // Kills the JVM
        System.exit(0);
    }

    // Called by an app state when it is finished.
    void nextAppState()
    {/*
        // This is a quick diagram of app states:
        // Init -> (Wait) -> Play <-> Menu
        // Menu does not currently exist

        // Removes the already attached state so a new one can be added
        stateManager.detach(currentAppState);

        // A boolean to track if we are actually adding a new app state, to avoid
        // a NullPointerException
        boolean newStateAttached;// = true;

        // Goes to  or Play after Init
        if(currentAppState instanceof PlayAppState)
        {
            newStateAttached = false;
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
        }*/
    }

    @Override
    public void messageReceived(Client source, Message m)
    {
        if(m instanceof NameRequestMessage)
        {
            NameRequestMessage nrm = (NameRequestMessage) m;
            if(nrm.name.equals(""))
            {
                nrm.name = UUID.randomUUID().toString().split("-")[0];
                client.send(nrm);
            }
            else
            {
                // the games have begun! < important stuff here when we are actually using appstates
            }
        }
    }

    private void initKeys()
    {
        inputManager.addMapping("Shoot", new KeyTrigger(Keyboard.KEY_SPACE), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Throttle+", new KeyTrigger(Keyboard.KEY_W));
        inputManager.addMapping("Throttle-", new KeyTrigger(Keyboard.KEY_S));
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf)
    {
        client.send(new InputEventMessage.Action(name, isPressed, tpf));
    }

    @Override
    public void onAnalog(String name, float value, float tpf)
    {
        client.send(new InputEventMessage.Analog(name, value, tpf));
    }
}

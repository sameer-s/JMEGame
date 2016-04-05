package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import javax.swing.JOptionPane;
import mygame.network.ClientMessageListener;
import mygame.network.ServerMain;
import mygame.network.message.ConnectionRequestMessage;
import mygame.network.message.ConnectionRequestMessage.ConnectionStatus;
import mygame.network.message.PlayerConnectionMessage;
import mygame.network.message.PlayerInformationMessage;

/**
 * The first application state.
 * The client makes its initial connection with the server.
 * @author Sameer Suri
 */
public class InitAppState extends AbstractAppState
{
    // When the app state first starts
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        // Has the superclass take care of some stuff
        super.initialize(stateManager, app);

        // Registers the custom message classes for networking
        Serializer.registerClasses(PlayerInformationMessage.class,
                                   ConnectionRequestMessage.class,
                                   PlayerConnectionMessage.class);

        // Asks the player for the address of the server with a simple dialog
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
            // Initializes the networking client
            Main.instance.client = Network.connectToServer(address, ServerMain.PORT);
            Main.instance.client.start();

            // Adds our listener so that we can recieve messages from the server
            Main.instance.clientMessageListener = new ClientMessageListener().setAppState(this);
            Main.instance.client.addMessageListener(Main.instance.clientMessageListener);

            // Requests the server for a connection
            Main.instance.client.send(new ConnectionRequestMessage().setReliable(true));
        }
        catch(IOException e)
        {
            // Reports any error that may occur to the log
            System.err.println("UNABLE TO CONNECT TO SERVER ON PORT: " + ServerMain.PORT);
            System.err.println("STACK TRACE:");
            e.printStackTrace(System.err);

            // Stops the app
            app.stop();

            // Informs the player that something happened.
            JOptionPane.showMessageDialog(
                null,
                "Unable to connect to the server.\nReason: " + e.getLocalizedMessage(),
                "Error: " + e.getClass().getName(),
                JOptionPane.ERROR_MESSAGE);

            // Kills the JVM
            System.exit(0);
        }
    }

    /**
     * Set the status of the player's connection.
     * This should be called by the message listener when the server sends over this information.
     * @param status The connection status
     */
    public void setStatus(ConnectionStatus status)
    {
        System.out.println("Status set: " + status.toString());

        // If it connected fine, set whether the player is player 1 or player 2
        switch(status)
        {
            case PLAYER1:
                Main.instance.isPlayer1 = true;
                break;
            case PLAYER2:
                Main.instance.isPlayer1 = false;
                break;
            // If the request was denied, this was because there were already two people on the server.
            // Notify the player and close the app.
            case REQUEST_DENIED:
                JOptionPane.showMessageDialog(
                        null,
                        "You cannot connect to the server because there were too many people.",
                        "Too many people",
                        JOptionPane.ERROR_MESSAGE);
                Main.instance.stop();
                break;
        }

        // Go to the next stage, which could be either waiting or playing, depending on if you're player 1 or player 2.
        Main.instance.nextAppState();
    }
}

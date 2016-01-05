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
    // Holds a reference to the app
    private Main app;

    // When the app state first starts
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        // Has the superclass take care of some stuff
        super.initialize(stateManager, app);

        // Casts the app to our Main app class and stores it for later use
        this.app = (Main) app;

        // Registers the custom message classes for networking
        Serializer.registerClasses(PlayerInformationMessage.class,
                                   ConnectionRequestMessage.class,
                                   PlayerConnectionMessage.class);

        String address = (String) JOptionPane.showInputDialog(null, "Enter the address of the server.\nBlank assumes 'localhost'.",
                "Server address?",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null);
        address = address == null ? "localhost" : address;

        try
        {
            // Initializes the networking client
            this.app.client = Network.connectToServer(address, ServerMain.PORT);
            this.app.client.start();

            // Adds our listener so that we can recieve messages from the server
            this.app.clientMessageListener = new ClientMessageListener().setAppState(this);
            this.app.client.addMessageListener(this.app.clientMessageListener);

            // Requests the server for a connection
            this.app.client.send(new ConnectionRequestMessage().setReliable(true));
        }
        catch(IOException e)
        {
            // Reports any error that may occur
            System.err.println("UNABLE TO CONNECT TO SERVER ON PORT: " + ServerMain.PORT);
            System.err.println("STACK TRACE:");
            e.printStackTrace(System.err);

            app.stop();
            JOptionPane.showMessageDialog(
                null,
                "Looks like we were unable to connect to the server.",
                "Error: " + e.getClass().getName(),
                JOptionPane.ERROR_MESSAGE);
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
                app.isPlayer1 = true;
                break;
            case PLAYER2:
                app.isPlayer1 = false;
                break;
            // If the request was denied, this was because there were already two people on the server.
            // Notify the player and close the app.
            case REQUEST_DENIED:
                JOptionPane.showMessageDialog(
                        null,
                        "Looks like there were already two people on the server. Sorry!",
                        "Too many people",
                        JOptionPane.ERROR_MESSAGE);
                app.stop();
                break;
        }

        // Go to the next stage, which could be either waiting or playing.
        app.nextAppState();
    }
}

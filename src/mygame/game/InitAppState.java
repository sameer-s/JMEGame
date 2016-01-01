package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import javax.swing.JOptionPane;
import mygame.Main;
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
    private Main app;

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;

        // Registers the custom message classes for networking
        Serializer.registerClasses(PlayerInformationMessage.class,
                                   ConnectionRequestMessage.class,
                                   PlayerConnectionMessage.class);

        try
        {
            // Initializes the networking client
            this.app.client = Network.connectToServer("localhost", ServerMain.PORT);
            this.app.client.start();

            this.app.clientMessageListener = new ClientMessageListener().setAppState(this);
            this.app.client.addMessageListener(this.app.clientMessageListener);

            this.app.client.send(new ConnectionRequestMessage().setReliable(true));
        }
        catch(IOException e)
        {
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

    private float t = 0;
    @Override
    public void update(float tpf)
    {
        t += tpf;
        System.out.println("Waiting for server response... t = " + t);
    }

    public void setStatus(ConnectionStatus status)
    {
        System.out.println("Status set: " + status.toString());

        switch(status)
        {
            case PLAYER1:
                app.isPlayer1 = true;
                break;
            case PLAYER2:
                app.isPlayer1 = false;
                break;
            case REQUEST_DENIED:
                JOptionPane.showMessageDialog(
                        null,
                        "Looks like there were already two people on the server. Sorry!",
                        "Too many people",
                        JOptionPane.ERROR_MESSAGE);
                app.stop();
                break;
        }

        app.nextAppState();
    }
}

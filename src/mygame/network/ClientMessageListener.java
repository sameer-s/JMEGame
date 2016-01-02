package mygame.network;

import com.jme3.app.state.AppState;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import mygame.game.InitAppState;
import mygame.game.PlayAppState;
import mygame.game.WaitAppState;
import mygame.network.message.ConnectionRequestMessage;
import mygame.network.message.PlayerConnectionMessage;
import mygame.network.message.PlayerInformationMessage;

/**
 * A listener that recieves messages from the server and takes care of the actions.
 * Note that this is the client listener, the server listener is part of the server main file.
 * @author Sameer Suri
 */
public class ClientMessageListener implements MessageListener<Client>
{
    // Holds a reference to the current app state, which provides context to the message
    private AppState appState;

    /**
     * Sets the current app state
     * @param appState The new app state
     * @return this, for chaining
     */
    public ClientMessageListener setAppState(AppState appState)
    {
        this.appState = appState;
        return this;
    }

    // Called when a message is recieved from the server
    @Override
    public void messageReceived(Client source, Message m)
    {
        // If we are initializing, and it is a connection request message, the server has assigned us a status
        if(m instanceof ConnectionRequestMessage && appState instanceof InitAppState)
        {
            // Notify the app state, which should take care of the rest
            ((InitAppState) appState).setStatus(((ConnectionRequestMessage) m).status);
        }
        // If we recieve a connection message and we're in the wait stage
        else if(m instanceof PlayerConnectionMessage && appState instanceof WaitAppState)
        {
            // And it's a connection (not a disconnection), and we are player 1
            if(((PlayerConnectionMessage) m).isConnection && ((WaitAppState) appState).isPlayer1())
            {
                // We are done waiting, there is a new player 2
                ((WaitAppState) appState).finish();
            }
        }
        // If we recieve a connection message while we are playing the game
        else if(m instanceof PlayerConnectionMessage && appState instanceof PlayAppState)
        {
            // And it is a disconnection
            if(!((PlayerConnectionMessage) m).isConnection)
            {
                // The other player has left, inform the app state
                ((PlayAppState) appState).opponentDisconnected();
            }
        }
        // If we are recieving info about the other player during the play stage
        else if(m instanceof PlayerInformationMessage && appState instanceof PlayAppState)
        {
            // Update the current location of the player in the Play app state
            ((PlayAppState) appState).updateOpponentLocation((PlayerInformationMessage) m);
        }
    }
}

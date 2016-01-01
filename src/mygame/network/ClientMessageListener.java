package mygame.network;

import com.jme3.app.state.AppState;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import mygame.game.InitAppState;
import mygame.game.PlayAppState;
import mygame.game.WaitingAppState;
import mygame.network.message.ConnectionRequestMessage;
import mygame.network.message.PlayerConnectionMessage;
import mygame.network.message.PlayerInformationMessage;

/**
 *
 * @author Sameer Suri
 */
public class ClientMessageListener implements MessageListener<Client>
{

    private AppState appState;

    public ClientMessageListener setAppState(AppState appState)
    {
        this.appState = appState;
        return this;
    }

    // When a message is recieved from the server
    @Override
    public void messageReceived(Client source, Message m)
    {
        if(m instanceof ConnectionRequestMessage && appState instanceof InitAppState)
        {
            ((InitAppState) appState).setStatus(((ConnectionRequestMessage) m).status);
        }
        else if(m instanceof PlayerConnectionMessage && appState instanceof WaitingAppState)
        {
            if(((PlayerConnectionMessage) m).isConnection && ((WaitingAppState) appState).isPlayer1())
            {
                ((WaitingAppState) appState).finish();
            }
        }
        else if(m instanceof PlayerConnectionMessage && appState instanceof PlayAppState)
        {
            if(!((PlayerConnectionMessage) m).isConnection)
            {
                ((PlayAppState) appState).opponentDisconnected();
            }
        }
        else if(m instanceof PlayerInformationMessage && appState instanceof PlayAppState)
        {
            ((PlayAppState) appState).updateOpponentLocation((PlayerInformationMessage) m);
        }
    }
}

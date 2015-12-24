/*
 * Copyright (C) 2015 Sameer Suri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mygame.network;

import com.jme3.app.state.AppState;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.Arrays;
import mygame.Main;
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

            PlayerInformationMessage pim = (PlayerInformationMessage)(m);

            System.out.println("Other player's status:");
            System.out.println("----------------------");
            System.out.println("Location: " + Arrays.toString(pim.location));
            System.out.println("Rotation: " + Arrays.toString(pim.rotation));
            System.out.println("Current Anim: " + pim.currentAnims[0]);


            // TODO: When done, make app final in PlayAppState
            Main app = ((PlayAppState) appState).app;
            System.out.println("\nNetwork controller status:");
            System.out.println("----------------------");
            System.out.println("Location: " + app.networkedController.getPhysicsLocation());
            System.out.println("Rotation: " + app.networkedController.getPhysicsRotation());
            System.out.println("Current Anim: not being logged");
        }
    }
}

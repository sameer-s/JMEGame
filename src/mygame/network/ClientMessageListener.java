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

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.Arrays;
import mygame.game.InitAppState;
import mygame.network.message.ConnectionRequestMessage;
import mygame.network.message.PlayerInformationMessage;

/**
 *
 * @author Sameer Suri
 */
public class ClientMessageListener implements MessageListener<Client>
{
    private InitAppState initAppState;

    public ClientMessageListener setInitAppState(InitAppState initAppState)
    {
        this.initAppState = initAppState;
        return this;
    }

    // When a message is recieved from the server
    @Override
    public void messageReceived(Client source, Message m)
    {
        if(m instanceof ConnectionRequestMessage)
        {
            initAppState.setStatus(((ConnectionRequestMessage) m).status);
        }
        else if(m instanceof PlayerInformationMessage)
        {
            PlayerInformationMessage pim = (PlayerInformationMessage)(m);

            System.out.println("Other player's status:");
            System.out.println("----------------------");
            System.out.println("Location: " + Arrays.toString(pim.location));
            System.out.println("Rotation: " + Arrays.toString(pim.rotation));
            System.out.println("Current Anim: " + pim.currentAnims[0]);
        }
    }
}

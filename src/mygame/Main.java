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

package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.network.Client;
import mygame.character.ThirdPersonCharacterControl;
import mygame.game.InitAppState;
import mygame.game.PlayAppState;

/**
 * The main class for my game. It has a number of purposes:
 * This is the starting point for the game.
 * Handles the init and update methods provided by the API.
 * Handles the controllers (i.e. player controller) used in this game.
 *
 * This is now implemented in AppStates, which can be found in the package: mygame.game
 *
 * Note that this is for the client application, not the server.
 * The server main class can be found at: mygame.network.ServerMain.java
 * @author Sameer Suri
 */
public class Main extends SimpleApplication
{
    /**
     * The player controller for the player controlled by THIS client
     */
    public ThirdPersonCharacterControl playerController;

    /**
     * Starting point for the application.
     * @param args The command line arguments passed
     */
    public static void main(String... args)
    {
        // Creates a new instance of our game and starts it.
        Main gp = new Main();
        gp.start();
    }

    /**
     * The networking client.
     */
    public Client client;

    public boolean isPlayer1;

    private AppState currentAppState;

    /**
     * Called by the engine in order to start the game.
     */
    @Override
    public void simpleInitApp()
    {
        // 2 game states:
        // Init -> Play
        currentAppState = new InitAppState();
        stateManager.attach(currentAppState);

        // Disables the default diagnostic
        setDisplayFps(false);
        setDisplayStatView(false);
    }

    /**
     * Cleans up resources, closes the window, and kills the app.
     */
    @Override
    public void destroy()
    {
        // Stops the networking to end the connection cleanly
        if(client != null)
            client.close();

        // Has the superclass finish cleanup
        super.destroy();
    }

    public void nextAppState()
    {
        // Init -> Play
        // IN THE FUTURE:
        // Play -> Menu
        // Menu -> Play

        stateManager.detach(currentAppState);

        if(currentAppState instanceof InitAppState)
        {
            currentAppState = new PlayAppState();
            stateManager.attach(currentAppState);
        }
    }
}

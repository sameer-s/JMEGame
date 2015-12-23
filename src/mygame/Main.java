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
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import org.lwjgl.input.Keyboard;

/**
 * The main class for my game. It has a number of purposes:
 * This is the starting point for the game.
 * Handles the init and update methods provided by the API.
 * Handles the controllers (i.e. player controller) used in this game.
 * @author Sameer Suri
 */
public class Main extends SimpleApplication
{
    // Holds a reference to our player controller
    private ThirdPersonCharacterControl playerController;

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
     * Called by the engine in order to start the game.
     */
    @Override
    public void simpleInitApp()
    {
        // Disables the default diagnostic
        setDisplayFps(false);
        setDisplayStatView(false);

        // Sets up the Bullet Physics Engine
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // Loads the model used for the scene
        Spatial sceneModel = assetManager
                .loadModel("Scenes/ManyLights/Main.scene");
        // Scales it to make it a little short
        sceneModel.scale(1f, .5f, 1f);
        // Generates a collider for physics collisions
        CollisionShape sceneShape = CollisionShapeFactory
                .createMeshShape(sceneModel);
        // Creates a control. What a control does is tell what it's controlling
        // what exactly to do. A Rigid Body Control is very simple: it's a rigid
        // body, meaning it does basically nothing. The rigid body is for the
        // scene, which needs not move.
        RigidBodyControl scene = new RigidBodyControl(sceneShape, 0);
        // Attaches the control to our model
        sceneModel.addControl(scene);
        // Attaches the model to the root node
        // This makes it appear in the world
        rootNode.attachChild(sceneModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(scene);

        // Loads the model for our player
        Spatial playerModel = assetManager
                .loadModel("Models/MainCharacter3_2/MainCharacter3_2.j3o");
        // Makes some adjustment so it works properly
        playerModel.scale(2.f);
        playerModel.rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);
        playerModel.setLocalTranslation(-5f, 2f, 5f);

        // Creates the chase camera. The chase camera is a camera which rotates
        // and zooms around the player. There are some configuration changes
        // I made, which I detail below:
        ChaseCamera chaseCam = new ChaseCamera(cam, playerModel, inputManager);
        // By default, you have to push down a mouse button to rotate the chase cam. This disables that.
        chaseCam.setDragToRotate(false);
        // By default, it looks at the player model's (0,0,0), which is at its feet. This looks a bit higher.
        chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0));
        // This keeps the camera a bit closer to the player than the default. This can be changed by the scroll wheel (on the mouse).
        chaseCam.setDefaultDistance(7f);
        // Speeds up the rotation, as the default is quite slow.
        chaseCam.setRotationSpeed(2f);

        // Maps the names that the Third Person Character Controller class uses
        // for animations to that the model uses.
        HashMap<String, String> anims = new HashMap<>();
        anims.put("Idle", "Idle");
        anims.put("Move", "Running3");

        // Creates our new character controller, passing in a few necessary parameters.
        playerController = new ThirdPersonCharacterControl(inputManager, anims, playerModel, cam);
        // Attaches the control to the player model
        playerModel.addControl(playerController);

        // Attaches the model to the root node
        // This makes it appear in the world
        rootNode.attachChild(playerModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(playerModel);

        // Creates a sun (a light) so that the player can see.
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-.1f, -.7f, -1f));
        rootNode.addLight(sun);

        // Registers the GUI-based console with the keyboard listeners
        GUIConsole console = new GUIConsole();
        console.initKeys(inputManager, new KeyTrigger(Keyboard.KEY_T));
    }

    /**
     * Called in a loop by the engine to allow the game to make changes.
     * @param tpf "Time per frame"; the amount of time taken between the last update cycle and this one
     */
    @Override
    public void simpleUpdate(float tpf)
    {
        // Updates the player controller, allowing it to move the player and handle animation changes.
        playerController.update(tpf);
    }

    /**
     * A simple logger. Will be removed.
     * @param tag A tag to log the data with
     * @param val The data
     */
    @SuppressWarnings("unused")
    private void log(Object tag, Object val)
    {
        System.out.println(tag.toString() + ": " + val.toString());
    }
}

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
package mygame.game;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import javax.swing.JOptionPane;
import mygame.Main;
import mygame.character.NetworkedCharacterAnimationHandler;
import mygame.character.NetworkedCharacterControl;
import mygame.character.ThirdPersonCharacterControl;
import static mygame.character.ThirdPersonCharacterControl._height;
import static mygame.character.ThirdPersonCharacterControl._mass;
import static mygame.character.ThirdPersonCharacterControl._radius;
import mygame.gui.GUIConsole;
import mygame.network.message.PlayerInformationMessage;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Sameer Suri
 */
public class PlayAppState extends AbstractAppState
{
    private Main app;

    // These are, for now, both the male model. This will be changed at some point.
    private static final String MALE_MODEL = "Models/MainCharacter3_2/MainCharacter3_2.j3o",
                              FEMALE_MODEL = "Models/MainCharacter3_2/MainCharacter3_2.j3o";

    public static HashMap<String, String> MALE_ANIMS, FEMALE_ANIMS;

    private void setupAnimMaps()
    {
        // Maps the names that the Third Person Character Controller class uses
        // for animations to that the model uses.

        // Male:
        MALE_ANIMS = new HashMap<>();
        MALE_ANIMS.put("Idle", "Idle");
        MALE_ANIMS.put("Move", "Running3");

        // Female: again, for now, the same. Will be changed.
        FEMALE_ANIMS = new HashMap<>();
        FEMALE_ANIMS.put("Idle", "Idle");
        FEMALE_ANIMS.put("Move", "Running3");
    }
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        this.app = (Main) app;

        // Sets up the Bullet Physics Engine
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // Loads the model used for the scene
        Spatial sceneModel = this.app.getAssetManager()
                .loadModel("Scenes/ManyLights/Main.scene");
        // Scales it to make it a little short
        sceneModel.scale(1f, .5f, 1f);
        // Generates a collider for physics collisions
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
        // Creates a control. What a control does is tell what it's controlling
        // what exactly to do. A Rigid Body Control is very simple: it's a rigid
        // body, meaning it does basically nothing. The rigid body is for the
        // scene, which needs not move.
        RigidBodyControl scene = new RigidBodyControl(sceneShape, 0);
        // Attaches the control to our model
        sceneModel.addControl(scene);
        // Attaches the model to the root node
        // This makes it appear in the world
        this.app.getRootNode().attachChild(sceneModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(scene);

        // Sets up the HashMaps we use for animation
        setupAnimMaps();

        Spatial playerModel = this.app.getAssetManager()
                .loadModel(this.app.isPlayer1 ? MALE_MODEL : FEMALE_MODEL);

        // Makes some adjustment so it works properly
        playerModel.scale(2.f);
        playerModel.rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);
        playerModel.setLocalTranslation(-5f, 2f, 5f);

        // Creates the chase camera. The chase camera is a camera which rotates
        // and zooms around the player. There are some configuration changes
        // I made, which I detail below:
        ChaseCamera chaseCam = new ChaseCamera(this.app.getCamera(), playerModel, this.app.getInputManager());
        // By default, you have to push down a mouse button to rotate the chase cam. This disables that.
        chaseCam.setDragToRotate(false);
        // By default, it looks at the player model's (0,0,0), which is at its feet. This looks a bit higher.
        chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0));
        // This keeps the camera a bit closer to the player than the default. This can be changed by the scroll wheel (on the mouse).
        chaseCam.setDefaultDistance(7f);
        // Speeds up the rotation, as the default is quite slow.
        chaseCam.setRotationSpeed(2f);


        // Creates our new character controller, passing in a few necessary parameters.
        this.app.playerController = new ThirdPersonCharacterControl(
                this.app.isPlayer1 ? MALE_ANIMS : FEMALE_ANIMS,
                playerModel, this.app.getCamera());

        this.app.playerController.initKeys(this.app.getInputManager(), this.app.isPlayer1 ? 0 : 1);

        // Attaches the control to the player model
        playerModel.addControl(this.app.playerController);

        ////////////////////////////////////////////////////
        this.app.otherPlayer = this.app.getAssetManager().loadModel(this.app.isPlayer1 ? FEMALE_MODEL : MALE_MODEL);
        this.app.otherPlayer.scale(2.f);
        this.app.otherPlayer.rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);
        this.app.otherPlayer.setLocalTranslation(-5f, 2f, 5f);

        this.app.getRootNode().attachChild(this.app.otherPlayer);
        this.app.otherPlayer.setLocalTranslation(-5f, 2f, 5f);
        NetworkedCharacterAnimationHandler.init(this.app.otherPlayer, this.app.isPlayer1 ? FEMALE_ANIMS : MALE_ANIMS);
        RigidBodyControl otherPlayerControl = new RigidBodyControl(_mass);
        otherPlayerControl.setCollisionShape(new CapsuleCollisionShape(_radius, _height));
        otherPlayerControl.setKinematic(true);
        this.app.otherPlayer.addControl(otherPlayerControl);
        ///////////////////////////////////////////////////

        // Loads the opposite model for your opponent
        Spatial opponentModel = this.app.getAssetManager()
                .loadModel(this.app.isPlayer1 ? FEMALE_MODEL : MALE_MODEL);
        // Makes some adjustment so it works properly

        opponentModel.scale(2.f);
        opponentModel.rotate(0f, 180f * FastMath.DEG_TO_RAD, 0f);
        opponentModel.setLocalTranslation(-5f, 200f, 5f);

        this.app.networkedController = new NetworkedCharacterControl(
                this.app.isPlayer1 ? FEMALE_ANIMS : MALE_ANIMS, opponentModel);
        opponentModel.addControl(this.app.networkedController);


        // Attaches the model to the root node
        // This makes it appear in the world
        this.app.getRootNode().attachChild(playerModel);
        // Registers the model with the Bullet Physics Engine
        bulletAppState.getPhysicsSpace().add(playerModel);

        // Creates a sun (a light) so that the player can see.
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-.1f, -.7f, -1f));
        this.app.getRootNode().addLight(sun);

        GUIConsole.CommandRunner commandRunner = (cmd) -> {
            // If nothing was inputted, do nothing!
            if(cmd == null) return;

            // Gets the first word in the command.
            switch(cmd.split(" ")[0])
            {
                default:
                    break;
            }
        };

        // Registers the GUI-based console
        GUIConsole console = new GUIConsole();
        console.initKeys(this.app.getInputManager(), new KeyTrigger(Keyboard.KEY_T), commandRunner);

        this.app.clientMessageListener.setAppState(this);
    }



    private Vector3f otherCharacterLocation = new Vector3f();
    private Quaternion otherCharacterRotation = new Quaternion();
    private String[] otherCharacterAnims = null;

    /**
     * Called in a loop by the engine to allow the game to make changes.
     * @param tpf "Time per frame"; the amount of time taken between the last update cycle and this one
     */
    @Override
    public void update(float tpf)
    {
        // Updates the player controller, allowing it to move the player and handle animation changes.
        app.playerController.update(tpf);

        app.client.send(app.playerController.toMessage());


        app.otherPlayer.setLocalTranslation(otherCharacterLocation);
        app.otherPlayer.setLocalRotation(otherCharacterRotation);
        if(otherCharacterAnims != null)
        {
            NetworkedCharacterAnimationHandler.updateAnims(otherCharacterAnims);
        }
    }

    public void opponentDisconnected()
    {
        app.stop();
        JOptionPane.showMessageDialog(
            null,
            "Looks like your opponent disconnected. Thanks for playing!",
            "Opponent disconnected",
            JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    public void updateOpponentLocation(PlayerInformationMessage m)
    {
        otherCharacterLocation = new Vector3f(m.location[0], m.location[1], m.location[2]);
//        otherCharacterRotation = new Quaternion(m.rotation[0], m.rotation[1], m.rotation[2], m.rotation[3]);
        otherCharacterAnims = m.currentAnims;
    }
}

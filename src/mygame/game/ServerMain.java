package mygame.game;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import mygame.debug.DebugLogger;
import mygame.game.message.NameRequestMessage;
import mygame.scene.DestructibleGhost;
import mygame.scene.character.RotationLockedChaseCamera;

/**
 * The main class for the client application. It has a number of purposes:
 * This is the starting point for the game.
 * Handles the app states that take care of the game
 * Handles the player controller and other player spatial for the characters.
 * *
 * @author Sameer Suri
 */
public class ServerMain extends SimpleApplication implements MessageListener<HostedConnection>, ConnectionListener
{
    /* Game stuff: */

    public static ServerMain instance;

    private Map<String, HostedConnection> players;
    private Map<String, Spatial> playerShips;

    private String name;

    // The networking server.
    Server server;

    public static void main(String... args)
    {
        ServerMain gp = new ServerMain();
        instance = gp;
        gp.start();
    }

    public ServerMain()
    {
        super(new AppState[] {});
    }

    // Called by the engine in order to start the game.
    @Override
    public void simpleInitApp()
    {
        settings.setUseJoysticks(true);

        this.setPauseOnLostFocus(false);

        inputManager.setCursorVisible(false);

        Serializer.registerClasses(Constants.Network.messageClasses);

        try
        {
            // Creates the server and starts it.
            server = Network.createServer(Constants.Network.PORT);
            server.start();

            // Adds the connection listener so we can be notified when a client connects or leaves
            server.addConnectionListener(this);
            // Allows this to recieve messages from clients
            server.addMessageListener(this);

            System.out.printf("Server hosted:%n\tIP=\"%s\"%n\tPort=\"%d\"%n", Inet4Address.getLocalHost().getHostAddress(), Constants.Network.PORT);
        }
        catch(IOException e)
        {
            // If an error occured, print it to the console, and exit.
            System.err.println("UNABLE TO START SERVER ON PORT: " + Constants.Network.PORT);
            System.err.println("STACK TRACE:");
            e.printStackTrace(System.err);
            stop();
            // Kill the JVM
            System.exit(0);
        }

        players = new HashMap<>();
        playerShips = new HashMap<>();

        name = UUID.randomUUID().toString().split("-")[0];

        players.put(name, null);
        Spatial playerModel = assetManager.loadModel(Constants.Scene.SHIP_MODEL);
        playerModel.setName("Player" + name);
//        playerModel.addControl(new GhostControl());
        playerShips.put(name, playerModel);

        rootNode.attachChild(playerModel);

        ChaseCamera camera = new RotationLockedChaseCamera(cam, playerModel, inputManager);

        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);

        for(int i = 0; i < 25; i++)
        {
            Geometry geom = new Geometry("RedBox", new Box(1f, 1f, 1f));
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            boxMat.setColor("Color", ColorRGBA.Red);
            geom.setLocalTranslation(i, i % 2 == 0 ? 25 - i : i, new Random().nextInt(25));
            geom.setMaterial(boxMat);
            geom.addControl(new DestructibleGhost(new BoxCollisionShape(new Vector3f(1f, 1f, 1f)), this, true));

            rootNode.attachChild(geom);
            stateManager.getState(BulletAppState.class).getPhysicsSpace().add(geom);
        }

        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Starfield.dds", EnvMapType.CubeMap));
    }

    // Cleans up resources, closes the window, and kills the app.
    @Override
    public void destroy()
    {
        // Stops the networking to end the connection cleanly
        if(server != null)
            server.close();

        DebugLogger.close();

        // Has the superclass finish cleanup
        super.destroy();

        // Kills the JVM
        System.exit(0);
    }

    // Called by an app state when it is finished.

    /**
     * Gets the settings established by the player in the settings menu.
     * @return The settings.
     */
    public AppSettings getSettings()
    {
        return settings;
    }

    public void addSpatial(Spatial sp, PhysicsControl... pcs)
    {
        rootNode.attachChild(sp);
        for(PhysicsControl pc : pcs)
        {
            sp.addControl(pc);
            this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(pc);
        }
    }

    public void addPhysicsTickListener(PhysicsTickListener... tickListeners)
    {
        for(PhysicsTickListener tickListener : tickListeners)
        {
           this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addTickListener(tickListener);
        }
    }

    public void removeSpatial(final Spatial spatial)
    {
        enqueue(() -> {
            try
            {
                spatial.removeFromParent();
                getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(spatial);

                while(spatial.getNumControls() > 0)
                {
                    spatial.removeControl(spatial.getControl(0));
                }

                nullifySpatial(spatial);
            }
            catch(NullPointerException npe) {}

            return 0;
        });
    }

    private void nullifySpatial(Spatial spatial)
    {
        spatial = null;
    }

    @Override
    public void messageReceived(HostedConnection source, Message m)
    {
        if(m instanceof NameRequestMessage)
        {
            NameRequestMessage nrm = (NameRequestMessage) m;
            if(nrm.name.equals("") || players.containsKey(nrm.name))
            {
                source.send(new NameRequestMessage());
            }
            else
            {
                players.put(nrm.name, source);
                Spatial playerModel = assetManager.loadModel(Constants.Scene.SHIP_MODEL);
                playerModel.setName("Player" + nrm.name);
                playerShips.put(nrm.name, playerModel);
            }
        }
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn)
    {
        conn.send(new NameRequestMessage());
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn)
    {

    }
}

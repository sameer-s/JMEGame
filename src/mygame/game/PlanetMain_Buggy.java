package mygame.game;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmeplanet.FractalDataSource;
import jmeplanet.Planet;
import jmeplanet.PlanetAppState;
import jmeplanet.PlanetCollisionShape;
import jmeplanet.test.Utility;
import mygame.scene.character.RotationLockedChaseCamera;
import mygame.scene.character.ShipCharacterControl;

/**
 *
 * @author Sameer
 */
public class PlanetMain_Buggy extends SimpleApplication
{
    public static PlanetMain_Buggy instance;
    
    BulletAppState bulletAppState;
    PlanetAppState planetAppState;
    
    private static final String SHIP_MODEL = "Models/ship/SpaceShip.j3o";
    
    public static void main(String... args)
    {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        
        PlanetMain_Buggy app = new PlanetMain_Buggy();
        
        instance = app;
        
        app.setSettings(settings);
        app.showSettings = true;
        app.start();
    }

    public PlanetMain_Buggy()
    {
        super(new StatsAppState(), new DebugKeysAppState());
    }
    
    @Override
    public void simpleInitApp()
    {
        setDisplayStatView(false);
        
        Logger.getLogger("com.jme3").setLevel(Level.SEVERE);
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun); 
    
        Node sceneNode = new Node("Scene");
        sceneNode.attachChild(Utility.createSkyBox(this.getAssetManager(), "Textures/blue-glow-1024.dds"));
        rootNode.attachChild(sceneNode);
        
        Node player = new Node("Player");
        Spatial playerModel = assetManager.loadModel(SHIP_MODEL);
        playerModel.setLocalTranslation(0, -2.5f, 0);
        playerModel.setName("PlayerModel");
        player.attachChild(playerModel);
        player.setLocalTranslation(new Vector3f(1100f, 0f, 0f));
        player.addControl(new ShipCharacterControl(player, "").initKeys(inputManager).registerMappings(inputManager));
        rootNode.attachChild(player);
        stateManager.getState(BulletAppState.class).getPhysicsSpace().add(player);
        RotationLockedChaseCamera chaseCam = new RotationLockedChaseCamera(cam, player, inputManager);
        
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Starfield.dds", SkyFactory.EnvMapType.CubeMap));

        planetAppState = new PlanetAppState(rootNode, sun);
        planetAppState.setShadowsEnabled(true);
        stateManager.attach(planetAppState);
        
        FractalDataSource planetDataSource = new FractalDataSource(4);
        planetDataSource.setHeightScale((800f/63710f) * 1000);
//        planetDataSource.setHeightScale(800f);
        Planet planet = Utility.createEarthLikePlanet(getAssetManager(), 1000.0f, null, planetDataSource);
        System.out.println(planet.getLocalTranslation());
        planet.addControl(new RigidBodyControl(new PlanetCollisionShape(planet.getLocalTranslation(), planet.getRadius(), planetDataSource), 0f));
        planetAppState.addPlanet(planet);
        rootNode.attachChild(planet);
        bulletAppState.getPhysicsSpace().add(planet);
                
        Geometry geom = new Geometry("BlueBox", new Box(1f, 1f, 1f));
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Blue);
        geom.setLocalTranslation(-49950, 0, 150000);
        geom.setMaterial(boxMat);
        geom.addControl(new RigidBodyControl());
        
        rootNode.attachChild(geom);
        bulletAppState.getPhysicsSpace().add(geom);
        
        bulletAppState.setDebugEnabled(true);
        
        rootNode.getChildren().forEach(a->System.out.println(a));
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
            }
            catch(NullPointerException npe) {}

            return 0;
        });
    }
}

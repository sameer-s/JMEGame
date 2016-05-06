package mygame.scene.character;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import mygame.game.Main;
import mygame.game.PlanetMain_Buggy;
import mygame.scene.DestructibleGameObject;
import mygame.util.Recursive;
import org.lwjgl.input.Keyboard;

/**
 * A class to control the ship.
 *
 * @author Sameer Suri
 */
public class ShipCharacterControl extends GhostControl implements ActionListener, DestructibleGameObject
{
    // Constants describing the movement of the character
    static final float maxSpeed = .05f;
    static final int throttleInc = 5;

    private int throttle = 0;

    private String playerCode;

    private ParticleEmitter trail;

    /**
     * Constructor for the control.
     * @param spatial The model used, so that the control can find the animations in the model
     * @param playerCode The custom code that corresponds to this player
     */
    public ShipCharacterControl(Spatial spatial, String playerCode)
    {
        super();

        setCollisionShape(generateShape(spatial));

        this.playerCode = playerCode;

        ColorRGBA startColor, endColor;

        if(playerCode.equals("")) //player 1
        {
            endColor = new ColorRGBA(1, 1, 0, 0f);
            startColor = new ColorRGBA(1, 0, 0, 0.5f);
        }
        else // player 2
        {
            endColor = new ColorRGBA(0, 1, 1, 0f);
            startColor = new ColorRGBA(0, 0, 1, 0.5f);
        }

        trail = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(PlanetMain_Buggy.instance.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", PlanetMain_Buggy.instance.getAssetManager().loadTexture("Effects/Explosion/flash.png"));
        trail.setMaterial(fireMat);
        trail.setImagesX(2); trail.setImagesY(2);
        trail.setEndColor(endColor);
        trail.setStartColor(startColor);
        trail.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        trail.setStartSize(1f);
        trail.setEndSize(0f);
        trail.setGravity(0f,0f,0f);
        trail.setLowLife(1.5f);
        trail.setHighLife(.5f);
        trail.getParticleInfluencer().setVelocityVariation(1f);
        trail.setLocalTranslation(0, -1.25f, 0);
    }

    /**
     * Binds keys to their respective actions.
     * Also takes care of joystick input.
     * @param inputManager The input manager that is uesd for binding the actions.
     * @return This object.
     */
    public ShipCharacterControl initKeys(InputManager inputManager)
    {
        // Binds keys to their respective actions

        inputManager.addMapping("Throttle+" + playerCode, new KeyTrigger(Keyboard.KEY_W));
        inputManager.addMapping("Throttle-" + playerCode, new KeyTrigger(Keyboard.KEY_S));

        inputManager.addMapping("Shoot" + playerCode, new MouseButtonTrigger(MouseInput.BUTTON_LEFT), new KeyTrigger(Keyboard.KEY_SPACE));

        return this;
    }

    public ShipCharacterControl registerMappings(InputManager inputManager)
    {
        String[] mappings = new String[] {
          "Throttle+",
          "Throttle-",
          "Shoot"
        };

        for(int i = 0; i < mappings.length; i++)
            mappings[i] += playerCode;

        inputManager.addListener(this, mappings);

        return this;
    }

    // Notifies the character controller when a button event occurs.
    @Override
    public void onAction(String action, boolean isPressed, float tpf)
    {
        if(matches(action, "Shoot") && isPressed)
        {
            makeBullet();
        }
        else if(matches(action, "Throttle+") && isPressed)
        {
            throttle += throttleInc;
        }
        else if(matches(action, "Throttle-") && isPressed)
        {
            throttle -= throttleInc;
        }
    }

    private boolean matches(String action, String toMatch)
    {
        return action.equals(toMatch) || action.equals(toMatch + playerCode);
    }
    // Handles movement as the game goes on.
    @Override
    public void update(float tpf)
    {
        // Has the superclass take care of physics stuff
        super.update(tpf);

        Vector3f targetLocation = this.spatial.getLocalTranslation()
                .add(this.spatial.getLocalRotation().getRotationColumn(2)
                .mult((((float) throttle) / 100f) * maxSpeed));

        this.spatial.setLocalTranslation(targetLocation);

       /* enables the trail if they are going fast enough */
       if(spatial instanceof Node && false /* disabled */)
       {
           if(throttle > 0)
           {
                ((Node) spatial).attachChild(trail);
           }
           else
           {
               ((Node) spatial).detachChild(trail);
           }
       }
     }

    public static CollisionShape generateShape(Spatial spatial)
    {
        List<Geometry> geometries = new ArrayList<>();

        Recursive<Consumer<Spatial>> findGeometries = new Recursive<>();

        findGeometries.function = (sp) -> {
            if(sp instanceof Geometry)
            {
                geometries.add((Geometry) sp);
            }
            else
            {
                for(Spatial child : ((Node) sp).getChildren()) findGeometries.function.accept(child);
            }
        };

        findGeometries.function.accept(spatial);

        CompoundCollisionShape shape = new CompoundCollisionShape();

        for(Geometry geom : geometries)
        {
            shape.addChildShape(new MeshCollisionShape(geom.getMesh()), new Vector3f(.15f, -1.5f, -.5f));
        }

        return shape;
    }

    int bulletNum = 0;
    public void makeBullet()
    {
        if(spatial == null) return;

        Vector3f size = new Vector3f(.1f, .05f, .4f);

        Geometry bullet = new Geometry("bullet" + bulletNum++, new Box(size.x, size.y, size.z));
        bullet.setLocalTranslation(this.spatial.getLocalTranslation().add(0, -1.5f, 0));

        Material mat = new Material(PlanetMain_Buggy.instance.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);

        bullet.setMaterial(mat);

        BulletControl bulletControl = new BulletControl(new BoxCollisionShape(size),
                this.spatial.getLocalRotation().getRotationColumn(2).normalize().mult(0.010f + (((throttle > 0 ? throttle : 0) * maxSpeed ) / 100f)),
                5f, playerCode);

        bullet.setLocalRotation(this.spatial.getLocalRotation());

        PlanetMain_Buggy.instance.addSpatial(bullet, bulletControl);
    }

    @Override
    public void destroyGameObject(Map<String, Object> data)
    {
        PlanetMain_Buggy.instance.enqueue(() -> {
//            PlanetMain.instance.playerLoses(playerCode);

            try
            {
                ColorRGBA startColor, endColor;

                if(playerCode.equals("")) //player 1
                {
                    endColor = new ColorRGBA(1, 1, 0, 0f);
                    startColor = new ColorRGBA(1, 0, 0, 0.5f);
                }
                else // player 2
                {
                    endColor = new ColorRGBA(0, 1, 1, 0f);
                    startColor = new ColorRGBA(0, 0, 1, 0.5f);
                }

                ParticleEmitter fireEffect = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
                Material fireMat = new Material(PlanetMain_Buggy.instance.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                fireMat.setTexture("Texture", PlanetMain_Buggy.instance.getAssetManager().loadTexture("Effects/Explosion/flame.png"));
                fireEffect.setMaterial(fireMat);
                fireEffect.setImagesX(2); fireEffect.setImagesY(2);
                fireEffect.setEndColor(endColor);
                fireEffect.setStartColor(startColor);
                fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
                fireEffect.setStartSize(3.6f);
                fireEffect.setEndSize(0f);
                fireEffect.setGravity(0f,0f,0f);
                fireEffect.setLowLife(1.5f);
                fireEffect.setHighLife(.5f);
                fireEffect.setLocalTranslation(spatial.getLocalTranslation());
                fireEffect.getParticleInfluencer().setVelocityVariation(1f);
                PlanetMain_Buggy.instance.getRootNode().attachChild(fireEffect);

                ParticleEmitter debrisEffect = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
                Material debrisMat = new Material(PlanetMain_Buggy.instance.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                debrisMat.setTexture("Texture", PlanetMain_Buggy.instance.getAssetManager().loadTexture("Effects/Explosion/Debris.png"));
                debrisEffect.setMaterial(debrisMat);
                debrisEffect.setImagesX(3); debrisEffect.setImagesY(3);
                debrisEffect.setRotateSpeed(4);
                debrisEffect.setSelectRandomImage(true);
                debrisEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 4, 0));
                debrisEffect.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
                debrisEffect.setGravity(0f,6f,0f);
                debrisEffect.setLocalTranslation(spatial.getLocalTranslation());
                debrisEffect.getParticleInfluencer().setVelocityVariation(1f);
                PlanetMain_Buggy.instance.getRootNode().attachChild(debrisEffect);
                debrisEffect.emitAllParticles();

                Timer t = new Timer();
                t.schedule(new TimerTask(){
                    @Override
                    public void run()
                    {
                        PlanetMain_Buggy.instance.removeSpatial(fireEffect);
                        PlanetMain_Buggy.instance.removeSpatial(debrisEffect);
                    }
                }, 1500L);


                spatial.removeFromParent();
                PlanetMain_Buggy.instance.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(spatial);

                spatial.removeControl(this);

                spatial = null;
            }
            catch(NullPointerException npe) {}

            return 0;
        });
    }

    @Override
    public boolean shouldBeDestroyed(Map<String, Object> data)
    {
        return !(data.getOrDefault("ObjectType", "No Type").equals("Bullet") &&
                data.getOrDefault("PlayerCode", "No Code").equals(playerCode));
    }

    @Override
    public Map<String, Object> getInfo()
    {
        HashMap info = new HashMap<>();
        info.put("ObjectType", "Player");
        return info;
    }
}

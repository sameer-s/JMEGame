package mygame.scene;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.Timer;
import java.util.TimerTask;
import mygame.game.Main;

/**
 *
 * @author Sameer Suri
 */
public class DestructibleGhost extends GhostControl implements mygame.scene.GameObject.Destructible
{
    private Main app;
    boolean explode;

    public DestructibleGhost(CollisionShape shape, Main app, boolean explode)
    {
        super(shape);
        this.explode = explode;
        this.app = app;
    }

    public DestructibleGhost(CollisionShape shape, Main app)
    {
        this(shape, app, false);
    }

    @Override
    public void destroyGameObject()
    {
        app.enqueue(() -> {
            try
            {
                if(explode)
                {
                    ParticleEmitter fireEffect = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
                    Material fireMat = new Material(Main.instance.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
                    fireMat.setTexture("Texture", Main.instance.getAssetManager().loadTexture("Effects/Explosion/flame.png"));
                    fireEffect.setMaterial(fireMat);
                    fireEffect.setImagesX(2); fireEffect.setImagesY(2); // 2x2 texture animation
                    fireEffect.setEndColor( new ColorRGBA(1f, 0f, 0f, 1f) );   // red
                    fireEffect.setStartColor( new ColorRGBA(1f, 1f, 0f, 0.5f) ); // yellow
                    fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
                    fireEffect.setStartSize(1.8f);
                    fireEffect.setEndSize(0.3f);
                    fireEffect.setGravity(0f,0f,0f);
                    fireEffect.setLowLife(0.5f);
                    fireEffect.setHighLife(3f);
                    fireEffect.setLocalTranslation(spatial.getLocalTranslation());
                    fireEffect.getParticleInfluencer().setVelocityVariation(0.3f);
                    Main.instance.getRootNode().attachChild(fireEffect);

                    Timer t = new Timer();
                    t.schedule(new TimerTask(){
                        @Override
                        public void run()
                        {
                            Main.instance.removeSpatial(fireEffect);
                        }
                    }, 150L);
                }

                spatial.removeFromParent();
                app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(spatial);

                spatial.removeControl(DestructibleGhost.class);

                spatial = null;
            }
            catch(NullPointerException npe) {}

            return 0;
        });
    }
}

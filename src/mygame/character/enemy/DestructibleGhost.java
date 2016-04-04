package mygame.character.enemy;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import mygame.game.Main;

/**
 *
 * @author Sameer Suri
 */
public class DestructibleGhost extends GhostControl implements DestructibleGameObject
{
    private Main app;

    public DestructibleGhost(CollisionShape shape, Main app)
    {
        super(shape);

        this.app = app;
    }

    @Override
    public void destroyGameObject()
    {
        app.enqueue(() -> {

            try
            {
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

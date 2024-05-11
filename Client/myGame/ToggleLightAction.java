package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import tage.physics.PhysicsObject;
import org.joml.*;

public class ToggleLightAction extends AbstractInputAction {
  private MyGame game;

  public ToggleLightAction(MyGame g) {
    game = g;
  }

   @Override
  public void performAction(float time, Event e) {
    boolean isLightEnabled = game.getIsLightEnabled();
    Light spotLight = game.getSpotLight();

    if (isLightEnabled) {
      spotLight.setType(Light.LightType.SPOTLIGHT);
      spotLight.setLocation(new Vector3f(3f, 2f, 4f));
      spotLight.setDirection(new Vector3f(1f, 1f, 1f));
    } else {
      spotLight.setType(Light.LightType.SPOTLIGHT);
      spotLight.setLocation(new Vector3f(3f, -2f, 4f));
      spotLight.setDirection(new Vector3f(1f, -1f, 1f));
    }
    
    game.toggleLightEnabled();
  } 
}

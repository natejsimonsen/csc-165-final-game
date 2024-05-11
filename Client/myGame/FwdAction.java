package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import tage.physics.PhysicsObject;
import org.joml.*;

public class FwdAction extends AbstractInputAction {
  private MyGame game;
  private GameObject av;
  private Vector3f oldPosition, newPosition;
  private Vector4f fwdDirection;

  public FwdAction(MyGame g) {
    game = g;
  }

  @Override
  public void performAction(float time, Event e) {
    av = game.getAvatar();
    oldPosition = av.getWorldLocation();
    fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
    fwdDirection.mul(av.getWorldRotation());
    float speed = 10f * time;
    if (e.getComponent().getIdentifier().equals(net.java.games.input.Component.Identifier.Key.S))
      speed *= -1;
    if (e.getComponent().getIdentifier().equals(net.java.games.input.Component.Identifier.Axis.X)) {
      float val = e.getValue();
      float deadzone = 0.2f;
      if (val < deadzone && val > deadzone)
        return;

      if (val > 0)
        speed *= -1;
    }
    fwdDirection.mul(speed);
    newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
    av.setLocalLocation(newPosition);
  }
}

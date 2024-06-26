package myGame;

import tage.*;
import tage.physics.PhysicsObject;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class TurnAction extends AbstractInputAction {
  private MyGame game;
  private GameObject av;
  private Vector4f oldUp;
  private Matrix4f rotAroundAvatarUp, oldRotation, newRotation;

  public TurnAction(MyGame g) {
    game = g;
    av = game.getAvatar();
  }

  @Override
  public void performAction(float time, Event e) {
    av = game.getAvatar();
    oldRotation = new Matrix4f(av.getWorldRotation());
    oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRotation);
    float rot = -2f * time;
    if (e.getComponent().getIdentifier().equals(net.java.games.input.Component.Identifier.Key.A))
      rot *= -1;
    if (e.getComponent().getIdentifier().equals(net.java.games.input.Component.Identifier.Axis.X)) {
      float val = e.getValue();
      float deadzone = 0.3f;
      if (val > -deadzone && val < deadzone)
        return;

      if (val < 0)
        rot *= -1;
    }
    rotAroundAvatarUp = new Matrix4f().rotation(rot, new Vector3f(0, 1, 0));
    newRotation = oldRotation;
    newRotation.mul(rotAroundAvatarUp);
    av.setLocalRotation(newRotation);
  }
  }

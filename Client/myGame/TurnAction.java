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
    float keyValue = e.getValue();
    if (keyValue > -.2 && keyValue < .2)
      return; // deadzone

    av = game.getAvatar();
    oldRotation = new Matrix4f(av.getWorldRotation());
    oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRotation);
    float rot = -2f * time;
    if (e.getComponent().getIdentifier().equals(net.java.games.input.Component.Identifier.Key.A))
      rot *= -1;
    rotAroundAvatarUp = new Matrix4f().rotation(rot, new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
    newRotation = oldRotation;
    newRotation.mul(rotAroundAvatarUp);
    av.setLocalRotation(newRotation);
  }
}

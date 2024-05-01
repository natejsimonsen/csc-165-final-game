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
  public void performAction(float time, net.java.games.input.Event evt) {
    PhysicsObject avatarPhysics = game.getAvatar().getPhysicsObject();

    if (avatarPhysics != null && avatarPhysics.isDynamic()) {
      float angularSpeed = -90.0f * time; 
      Vector3f angularVelocity = new Vector3f(0, angularSpeed, 0); 

      if (evt.getComponent().getIdentifier().equals(net.java.games.input.Component.Identifier.Key.A)) {
        angularVelocity.y = -angularVelocity.y;
      }

      avatarPhysics.setAngularVelocity(new float[] {
          angularVelocity.x,
          angularVelocity.y,
          angularVelocity.z
      });
    }
  }

  // @Override
  // public void performAction(float time, Event e) {
  //   float keyValue = e.getValue();
  //   if (keyValue > -.2 && keyValue < .2)
  //     return; // deadzone

  //   av = game.getAvatar();
  //   oldRotation = new Matrix4f(av.getWorldRotation());
  //   oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRotation);
  //   rotAroundAvatarUp = new Matrix4f().rotation(-2f * time, new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
  //   newRotation = oldRotation;
  //   newRotation.mul(rotAroundAvatarUp);
  //   av.setLocalRotation(newRotation);
  // }
}

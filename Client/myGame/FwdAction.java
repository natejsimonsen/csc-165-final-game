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
    fwdDirection.mul(speed);
    newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
    av.setLocalLocation(newPosition);
  } 

  // @Override
  // public void performAction(float time, net.java.games.input.Event evt) {
  //   GameObject avatar = game.getAvatar();
  //   PhysicsObject avatarPhysics = avatar.getPhysicsObject();

  //   if (avatarPhysics != null && avatarPhysics.isDynamic()) {
  //     Vector3f velocity = new Vector3f(0, 0, -10);
  //     Matrix4f rotationMatrix = avatar.getWorldRotation();
  //     Quaternionf rotationQuaternion = new Quaternionf();
  //     rotationMatrix.getNormalizedRotation(rotationQuaternion);
  //     velocity.rotate(rotationQuaternion);
  //     avatarPhysics.setLinearVelocity(new float[] {
  //         velocity.x,
  //         velocity.y,
  //         velocity.z
  //     });

  //     game.getProtClient().sendMoveMessage(avatar.getWorldLocation());
  //   }
  // }
}

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
  public void performAction(float time, net.java.games.input.Event evt) {
    GameObject avatar = game.getAvatar();
    PhysicsObject avatarPhysics = avatar.getPhysicsObject();

    if (avatarPhysics != null && avatarPhysics.isDynamic()) {
      Vector3f velocity = new Vector3f(0, 0, -10);
      Matrix4f rotationMatrix = avatar.getWorldRotation();
      Quaternionf rotationQuaternion = new Quaternionf();
      rotationMatrix.getNormalizedRotation(rotationQuaternion);
      velocity.rotate(rotationQuaternion);
      avatarPhysics.setLinearVelocity(new float[] {
          velocity.x,
          velocity.y,
          velocity.z
      });

      game.getProtClient().sendMoveMessage(av.getWorldLocation());
    }
  }

  // @Override
  // public void performAction(float time, net.java.games.input.Event evt) {
  // // Retrieve the physics object
  // if (avatarPhysics != null) {
  // // Apply a forward impulse or force
  // Vector3f direction = new Vector3f(0, 0, 1); // Forward direction
  // direction.mul(game.getAvatar().getWorldRotation()); // Adjust to current
  // rotation
  // direction.mul(10 * time); // Scale by a speed factor and delta time
  // avatarPhysics.applyImpulse(direction.x(), direction.y(), direction.z());
  // }
  // }
}

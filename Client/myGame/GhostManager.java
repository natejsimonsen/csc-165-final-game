package myGame;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.physics.PhysicsObject;
import tage.audio.*;

public class GhostManager {
  private MyGame game;
  private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();
  private Sound ghostWalkSound;

  public GhostManager(VariableFrameRateGame vfrg) {
    game = (MyGame) vfrg;
  }

  public void createGhostAvatar(UUID id, Vector3f position, String avatarName, String textureName) throws IOException {
    System.out.println("adding ghost with ID --> " + id);
    ObjShape s = game.getShape(avatarName);
    TextureImage t = game.getTexture(textureName);
    GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
    Matrix4f initialScale = (new Matrix4f()).scaling(0.25f);
    newAvatar.setLocalScale(initialScale);

    float mass = 1.0f;
    float up[] = { 0, 1, 0 };
    float radius = 0.75f;
    float height = 1.0f;
    double[] tempTransform;
    float [] vals = new float[16];

    Matrix4f translation = new Matrix4f(newAvatar.getLocalTranslation());
    tempTransform = game.toDoubleArray(translation.get(vals));
    PhysicsObject avatarP = (game.getEngine().getSceneGraph()).addPhysicsCapsuleX(
        mass, tempTransform, radius, height);
    avatarP.setBounciness(1.0f);
    avatarP.setDamping(0.8f, 0.8f);
    avatarP.setSleepThresholds(0.05f, 0.05f); // Low sleep thresholds
    newAvatar.setPhysicsObject(avatarP);
    ghostAvatars.add(newAvatar);

    ghostWalkSound = game.getGhostSound();
    ghostWalkSound.setLocation(newAvatar.getWorldLocation());
    ghostWalkSound.play();
  }

  public void removeGhostAvatar(UUID id) {
    GhostAvatar ghostAvatar = findAvatar(id);
    if (ghostAvatar != null) {
      game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
      ghostAvatars.remove(ghostAvatar);
    } else {
      System.out.println("tried to remove, but unable to find ghost in list");
    }
  }

  private GhostAvatar findAvatar(UUID id) {
    GhostAvatar ghostAvatar;
    Iterator<GhostAvatar> it = ghostAvatars.iterator();
    while (it.hasNext()) {
      ghostAvatar = it.next();
      if (ghostAvatar.getID().compareTo(id) == 0) {
        return ghostAvatar;
      }
    }
    return null;
  }

  public void updateGhostAvatar(UUID id, Vector3f position) {
    GhostAvatar ghostAvatar = findAvatar(id);
    if (ghostAvatar != null) {
      ghostAvatar.setPosition(position);
      ghostWalkSound.setLocation(ghostAvatar.getWorldLocation());
    } else {
      System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
    }
  }
}

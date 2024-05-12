package myGame;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import tage.physics.PhysicsObject;
import org.joml.*;
import tage.shapes.*;

public class ToggleAnimationAction extends AbstractInputAction {
  private MyGame game;

  public ToggleAnimationAction(MyGame g) {
    game = g;
  }

  @Override
  public void performAction(float time, Event e) {
    if (game.getAnimatedShape() == false)
      return;

    game.toggleAnimation();

    AnimatedShape s = (AnimatedShape) game.getAvatar().getShape();
    if (game.getAnimationEnabled()) {
      s.stopAnimation();
      s.playAnimation("WALK", 0.5f, AnimatedShape.EndType.LOOP, 0);
    } else {
      s.stopAnimation();
    }
  }
}

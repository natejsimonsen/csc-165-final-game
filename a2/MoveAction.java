package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction {
  MyGame game;
  double speed;
  boolean isGamepad;

  public MoveAction(MyGame game, double speed, boolean isGamepad) {
    super();
    this.game = game;
    this.speed = speed;
    this.isGamepad = isGamepad;
  }

  @Override
  public void performAction(float time, Event evt) {
    int direction = 1;
    float val = evt.getValue();
    if (isGamepad && val < 0.2 && val > -0.2)
      return;

    if (isGamepad && val > 0)
      direction = -1;

    game.moveDolphin((float)speed * (float)game.deltaTime * direction); 
  }
}



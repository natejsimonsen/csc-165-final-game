package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction {
  MyGame game;
  double speed;

  public MoveAction(MyGame game, double speed) {
    super();
    this.game = game;
    this.speed = speed;
  }

  @Override
  public void performAction(float time, Event evt) {
    float val = evt.getValue();
    int direction = -1;
    if (val < 0)
      direction = 1;
    if (val == 1)
      direction = 1;
    game.moveDolphin((float)speed * (float)game.deltaTime * direction);
  }
}



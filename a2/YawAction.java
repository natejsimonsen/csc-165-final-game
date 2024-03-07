package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class YawAction extends AbstractInputAction {
  MyGame game;
  float speed;
  boolean isGamePad;

  public YawAction(MyGame game, double speed, boolean isGamePad) {
    super();
    this.game = game;
    this.speed = (float)speed;
    this.isGamePad = isGamePad;
  }

  @Override
  public void performAction(float time, Event evt) {
    int direction = 1;
    float val = evt.getValue();
    if (isGamePad && val < 0.2 && val > -0.2)
      return;

    if (isGamePad && val > 0)
      direction = -1;

    game.dol.yaw(speed * (float)time * direction); 
  }
}

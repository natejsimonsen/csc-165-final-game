package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class PitchAction extends AbstractInputAction {
  MyGame game;
  double speed;
  boolean isGamePad;

  public PitchAction(MyGame game, double speed, boolean isGamePad) {
    super();
    this.game = game;
    this.speed = speed;
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


    game.dol.pitch((float)speed * (float)game.deltaTime * direction); 
  }
}

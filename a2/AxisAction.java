package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class AxisAction extends AbstractInputAction {
  MyGame game;

  public AxisAction(MyGame game) {
    super();
    this.game = game;
  }

  @Override
  public void performAction(float time, Event evt) {
    game.toggleAxisLines();
  }
}

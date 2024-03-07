package a2;

import net.java.games.input.Event;
import org.joml.*;
import tage.*;
import tage.input.action.AbstractInputAction;

public class ZoomAction extends AbstractInputAction {

  Camera cam;
  int direction;

  public ZoomAction(Camera cam, int direction) {
    super();
    this.cam = cam;
    this.direction = direction;
  }

  @Override
  public void performAction(float time, Event evt) {
    float speed = 30f;
    Vector3f cameraLocation = cam.getLocation();
    Vector3f newLocation = new Vector3f();
    newLocation
      .add(cameraLocation)
      .add(new Vector3f(0, time * speed * direction, 0));
    if (newLocation.y > 2) cam.setLocation(newLocation);
  }
}

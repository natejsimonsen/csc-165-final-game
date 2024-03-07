package a2;

import net.java.games.input.Event;
import org.joml.*;
import tage.*;
import tage.input.action.AbstractInputAction;

public class PanAction extends AbstractInputAction {

  Camera cam;
  String direction;

  public PanAction(Camera cam, String direction) {
    super();
    this.cam = cam;
    this.direction = direction;
  }

  @Override
  public void performAction(float time, Event evt) {
    float speed = 30f;
    Vector3f cameraLocation = cam.getLocation();
    Vector3f newLocation = new Vector3f();
    if (direction == "left") newLocation
      .add(cameraLocation)
      .add(new Vector3f(time * speed * -1, 0, 0));
    if (direction == "right") newLocation
      .add(cameraLocation)
      .add(new Vector3f(time * speed, 0, 0));
    if (direction == "up") newLocation
      .add(cameraLocation)
      .add(new Vector3f(0, 0, time * speed * -1));
    if (direction == "down") newLocation
      .add(cameraLocation)
      .add(new Vector3f(0, 0, time * speed));
    cam.setLocation(newLocation);
  }
}

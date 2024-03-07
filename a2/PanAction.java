package a2;

import net.java.games.input.Event;
import org.joml.*;
import tage.*;
import tage.input.action.AbstractInputAction;

public class PanAction extends AbstractInputAction {

  Camera cam;
  String direction;
  float speed = 30f;
  float time;
  Vector3f newLocation;

  public PanAction(Camera cam, String direction) {
    super();
    this.cam = cam;
    this.direction = direction;
  }

  private void moveLeft() {
    Vector3f cameraLocation = cam.getLocation();
    newLocation = new Vector3f();
    newLocation
      .add(cameraLocation)
      .add(new Vector3f(time * speed * -1, 0, 0));
  }
  
  private void moveRight() {
    Vector3f cameraLocation = cam.getLocation();
    newLocation = new Vector3f();
    newLocation
      .add(cameraLocation)
      .add(new Vector3f(time * speed, 0, 0));
  }
  
  private void moveUp() {
    Vector3f cameraLocation = cam.getLocation();
    newLocation = new Vector3f();
    newLocation
      .add(cameraLocation)
      .add(new Vector3f(0, 0, time * speed * -1));
  }

  private void moveDown() {
    Vector3f cameraLocation = cam.getLocation();
    newLocation = new Vector3f();
    newLocation
      .add(cameraLocation)
      .add(new Vector3f(0, 0, time * speed));
  }

  @Override
  public void performAction(float time, Event evt) {
    this.time = time;
    if (direction == "left") moveLeft(); 
    if (direction == "right") moveRight();
    if (direction == "up") moveUp();
    if (direction == "down") moveDown();

    if (direction == "pad") {
      float val = evt.getValue();
      if (val == 1) moveLeft();
      if (val == 0.5f) moveRight();
      if (val == 0.25f) moveUp();
      if (val == 0.75f) moveDown();
    }
    cam.setLocation(newLocation);
  }
}

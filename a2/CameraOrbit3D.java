package a2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Math;
import javax.swing.*;
import net.java.games.input.Event;
import org.joml.*;
import tage.*;
import tage.input.*;
import tage.input.action.AbstractInputAction;

public class CameraOrbit3D {

  private Engine engine;
  private Camera camera; // the camera being controlled
  private GameObject avatar; // the target avatar the camera looks at
  private float cameraAzimuth; // rotation around target Y axis
  private float cameraElevation; // elevation of camera above target
  private float cameraRadius; // distance between camera and target
  private float elevationMin = -90f;
  private float elevationMax = 90f;

  public CameraOrbit3D(Camera cam, GameObject av, Engine e) {
    engine = e;
    camera = cam;
    avatar = av;
    cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
    cameraElevation = 20.0f; // elevation is in degrees
    cameraRadius = 3.0f; // distance from camera to avatar
    setupInputs();
    updateCameraPosition();
  }

  private void setupInputs() {
    InputManager im = engine.getInputManager();

    OrbitAzimuthAction azmAction = new OrbitAzimuthAction(0);
    OrbitAzimuthAction azmActionLeft = new OrbitAzimuthAction(-1);
    OrbitAzimuthAction azmActionRight = new OrbitAzimuthAction(1);
    im.associateActionWithAllGamepads(
      net.java.games.input.Component.Identifier.Axis.RX,
      azmAction,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.RIGHT,
      azmActionLeft,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.LEFT,
      azmActionRight,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );

    OrbitRadiusAction radiusAction = new OrbitRadiusAction(0);
    OrbitRadiusAction radiusActionOut = new OrbitRadiusAction(1);
    OrbitRadiusAction radiusActionIn = new OrbitRadiusAction(-1);
    im.associateActionWithAllGamepads(
      net.java.games.input.Component.Identifier.Axis.RX,
      radiusAction,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.I,
      radiusActionIn,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.O,
      radiusActionOut,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );

    OrbitElevationAction elevationAction = new OrbitElevationAction(0);
    OrbitElevationAction elevationActionUp = new OrbitElevationAction(1);
    OrbitElevationAction elevationActionDown = new OrbitElevationAction(-1);
    im.associateActionWithAllGamepads(
      net.java.games.input.Component.Identifier.Axis.RX,
      radiusAction,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.UP,
      elevationActionUp,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.DOWN,
      elevationActionDown,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
  }

  // Compute the camera’s azimuth, elevation, and distance, relative to
  // the target in spherical coordinates, then convert to world Cartesian
  // coordinates and set the camera position from that.
  public void updateCameraPosition() {
    Vector3f avatarRot = avatar.getWorldForwardVector();
    double avatarAngle = Math.toDegrees(
      (double) avatarRot.angleSigned(
        new Vector3f(0, 0, -1),
        new Vector3f(0, 1, 0)
      )
    );
    float totalAz = cameraAzimuth - (float) avatarAngle;
    double theta = Math.toRadians(totalAz);
    double phi = Math.toRadians(cameraElevation);
    float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
    float y = cameraRadius * (float) (Math.sin(phi));
    float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));
    camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
    camera.lookAt(avatar);
  }

  private class OrbitAzimuthAction extends AbstractInputAction {

    private int direction;

    public OrbitAzimuthAction(int direction) {
      super();
      this.direction = direction;
    }

    public void performAction(float time, Event event) {
      float speed = 100f * time;
      double threshhold = 0.2;
      float rotAmount = 0.0f;

      if (event.getValue() < -threshhold) rotAmount = -speed;
      if (event.getValue() > threshhold) rotAmount = speed;

      if (direction == 1 || direction == -1) rotAmount = speed * direction;

      cameraAzimuth += rotAmount;
      cameraAzimuth = cameraAzimuth % 360;
      updateCameraPosition();
    }
  }

  private class OrbitRadiusAction extends AbstractInputAction {

    private int direction;

    public OrbitRadiusAction(int direction) {
      super();
      this.direction = direction;
    }

    public void performAction(float time, Event event) {
      float speed = 10f * time;
      double threshhold = 0.2;
      float radiusAmount = 0.0f;

      if (event.getValue() < -threshhold) radiusAmount = -speed;
      if (event.getValue() > threshhold) radiusAmount = speed;

      if (direction == 1 || direction == -1) radiusAmount = speed * direction;

      cameraRadius += radiusAmount;
      cameraRadius = Math.max(cameraRadius, 3.0f);
      updateCameraPosition();
    }
  }

  private class OrbitElevationAction extends AbstractInputAction {

    private int direction;

    public OrbitElevationAction(int direction) {
      super();
      this.direction = direction;
    }

    public void performAction(float time, Event event) {
      float speed = 50f * time;
      double threshhold = 0.2;
      float elevationAmount = 0.0f;

      if (event.getValue() < -threshhold) elevationAmount = -speed;
      if (event.getValue() > threshhold) elevationAmount = speed;

      if (direction == 1 || direction == -1) elevationAmount =
        speed * direction;

      float newCameraElevation = cameraElevation + elevationAmount;
      if (
        newCameraElevation > elevationMax || newCameraElevation < elevationMin
      ) elevationAmount = 0.0f;

      cameraElevation += elevationAmount;
      updateCameraPosition();
    }
  }

  public void setElevationMin(float min) {
    elevationMin = min;
  }

  public void setElevationMax(float max) {
    elevationMax = max;
  }

  public void setRelativeAzimuth(float amount) {
    cameraAzimuth += amount;
    updateCameraPosition();
  }

  public void setElevation(float amount) {
    cameraElevation = amount;
  }

  public void setRadius(float amount) {
    cameraRadius = amount;
  }
}

package a2;

import a2.CameraOrbit3D;
import a2.ManualFridge;
import a2.YawAction;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Math;
import javax.swing.*;
import org.joml.*;
import tage.*;
import tage.input.*;
import tage.shapes.*;

public class MyGame extends VariableFrameRateGame {

  private static Engine engine;
  protected boolean lost = false;
  protected boolean won = false;
  protected double lastFrameTime, currFrameTime, deltaTime;
  protected double speed = 4.5;
  protected GameObject dol, xAxis, yAxis, zAxis, ground;
  protected ObjShape dolS, xLine, yLine, zLine, plane;
  protected TextureImage dolTex, moonTex;
  protected Light light1;
  protected Camera cam, rightCamera;
  protected GameObject[] magnets;
  protected CameraOrbit3D camCtrl;

  public MyGame() {
    super();
  }

  public static void main(String[] args) {
    MyGame game = new MyGame();
    engine = new Engine(game);
    game.initializeSystem();
    game.game_loop();
  }

  @Override
  public void createViewports() {
    super.createViewports();
    engine.getRenderSystem().addViewport("RIGHT", .75f, 0, .25f, .25f);
    Viewport rightVp = engine.getRenderSystem().getViewport("RIGHT");
    rightCamera = rightVp.getCamera();
    rightVp.setHasBorder(true);
    rightVp.setBorderWidth(2);
    rightVp.setBorderColor(0.0f, 1.0f, 0.0f);
    rightCamera.setLocation(new Vector3f(0, 8, 0));
    rightCamera.setU(new Vector3f(1, 0, 0));
    rightCamera.setV(new Vector3f(0, 0, -1));
    rightCamera.setN(new Vector3f(0, -1, 0));
  }

  @Override
  public void loadShapes() {
    xLine = new Line(new Vector3f(0, 0, 0), new Vector3f(5, 0, 0));
    yLine = new Line(new Vector3f(0, 0, 0), new Vector3f(0, 5, 0));
    zLine = new Line(new Vector3f(0, 0, 0), new Vector3f(0, 0, 5));
    plane = new Plane();
    dolS = new ImportedModel("dolphinHighPoly.obj");
  }

  @Override
  public void loadTextures() {
    dolTex = new TextureImage("Dolphin_HighPolyUV.png");
    moonTex = new TextureImage("moon.jpg");
  }

  @Override
  public void buildObjects() {
    Matrix4f initialTranslation, initialScale;

    xAxis = new GameObject(GameObject.root(), xLine);
    yAxis = new GameObject(GameObject.root(), yLine);
    zAxis = new GameObject(GameObject.root(), zLine);

    xAxis.getRenderStates().setColor(new Vector3f(255, 0, 0));
    yAxis.getRenderStates().setColor(new Vector3f(0, 255, 0));
    zAxis.getRenderStates().setColor(new Vector3f(0, 0, 255));

    dol = new GameObject(GameObject.root(), dolS, dolTex);
    initialTranslation = (new Matrix4f()).translation(0, 0, 0);
    initialScale = (new Matrix4f()).scaling(3.0f);
    dol.setLocalTranslation(initialTranslation);
    dol.setLocalScale(initialScale);

    ground = new GameObject(GameObject.root(), plane, moonTex);
    initialTranslation = (new Matrix4f()).translation(0, -1, 0);
    initialScale = (new Matrix4f()).scaling(30.0f);
    ground.setLocalTranslation(initialTranslation);
    ground.setLocalScale(initialScale);
  }

  @Override
  public void initializeLights() {
    Light.setGlobalAmbient(.62f, .62f, .6f);
    light1 = new Light();
    light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
    (engine.getSceneGraph()).addLight(light1);
  }

  @Override
  public void initializeGame() {
    lastFrameTime = System.currentTimeMillis();
    currFrameTime = System.currentTimeMillis();
    (engine.getRenderSystem()).setWindowDimensions(1920, 1080);
    setupMainCamera();
    setupActions();
  }

  private void toggleAxisLines() {
    RenderStates x = xAxis.getRenderStates();
    RenderStates y = yAxis.getRenderStates();
    RenderStates z = zAxis.getRenderStates();
    boolean renderingEnabled = xAxis.getRenderStates().renderingEnabled();

    if (renderingEnabled) {
      x.disableRendering();
      y.disableRendering();
      z.disableRendering();
      return;
    }

    x.enableRendering();
    y.enableRendering();
    z.enableRendering();
  }

  private void setupMainCamera() {
    cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
    camCtrl = new CameraOrbit3D(cam, dol, engine);
    camCtrl.setElevationMin(2f);
    camCtrl.setRadius(5f);
  }

  private void setupActions() {
    InputManager im = engine.getInputManager();

    // yaw the dolphin
    YawAction yawRight = new YawAction(this, -speed / 2, false);
    YawAction yawLeft = new YawAction(this, speed / 2, false);
    YawAction yawGamePad = new YawAction(this, speed / 2, true);
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.A,
      yawLeft,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.D,
      yawRight,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllGamepads(
      net.java.games.input.Component.Identifier.Axis.X,
      yawGamePad,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );

    // move the dolphin
    MoveAction moveForward = new MoveAction(this, speed);
    MoveAction moveBackward = new MoveAction(this, -speed);
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.W,
      moveForward,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.S,
      moveBackward,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllGamepads(
      net.java.games.input.Component.Identifier.Axis.Y,
      moveForward,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );

    setupRightCameraActions(im);
  }

  private void setupRightCameraActions(InputManager im) {
    PanAction panLeft = new PanAction(rightCamera, "left");
    PanAction panRight = new PanAction(rightCamera, "right");
    PanAction panUp = new PanAction(rightCamera, "up");
    PanAction panDown = new PanAction(rightCamera, "down");
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.J,
      panLeft,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.L,
      panRight,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.I,
      panUp,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.K,
      panDown,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );

    ZoomAction zoomIn = new ZoomAction(rightCamera, -1);
    ZoomAction zoomOut = new ZoomAction(rightCamera, 1);
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key._0,
      zoomIn,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key._9,
      zoomOut,
      InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
    );
  }

  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_X:
        toggleAxisLines();
        break;
    }
    super.keyPressed(e);
  }

  @Override
  public void update() {
    engine.getInputManager().update((float) deltaTime);
    setTimes();
    camCtrl.updateCameraPosition();
  }

  public void setTimes() {
    lastFrameTime = currFrameTime;
    currFrameTime = System.currentTimeMillis();
    deltaTime = (currFrameTime - lastFrameTime) / 1000.0;
  }

  public void moveDolphin(float speed) {
    Vector3f loc, fwd, newLocation;
    fwd = dol.getWorldForwardVector();
    loc = dol.getWorldLocation();
    newLocation = loc.add(fwd.mul(speed));
    dol.setLocalLocation(newLocation);
  }
}

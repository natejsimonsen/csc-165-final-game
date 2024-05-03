package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.net.InetAddress;

import java.net.UnknownHostException;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.audio.*;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame {
  private static Engine engine;
  private InputManager im;
  private GhostManager gm;
  private String avatarName = "squareGuy";
  private String textureName = "squareGuy";
  private PhysicsEngine physicsEngine;

  private int counter = 0;
  private Vector3f currentPosition;
  private Matrix4f initialTranslation, initialRotation, initialScale;
  private double lastFrameTime, currFrameTime, deltaTime, elapsedTime = 0;

  private PhysicsObject terrP, avatarP;
  private GameObject terr, avatar, x, y, z;
  private ObjShape terrS, linxS, linyS, linzS;
  private TextureImage hills, brick;
  private Light light;
  private HashMap<String, TextureImage> playerTextures;
  private HashMap<String, ObjShape> playerShapes;
  private CameraOrbit3D camCtrl;
  private Camera cam;
  private IAudioManager audioMgr;
  private Sound bgSound, ghostSound;

  private String serverAddress;
  private int serverPort;
  private ProtocolType serverProtocol;
  private ProtocolClient protClient;
  private boolean isClientConnected = false;
  private int sky;
  private float vals[] = new float[16];

  public MyGame(String serverAddress, int serverPort, String protocol) {
    super();
    gm = new GhostManager(this);
    this.serverAddress = serverAddress;
    this.serverPort = serverPort;
    this.serverProtocol = ProtocolType.UDP;
  }

  public static void main(String[] args) {
    MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
    engine = new Engine(game);
    game.initializeSystem();
    game.game_loop();
  }

  @Override
  public void loadShapes() {
    playerShapes = new HashMap<>();
    terrS = new TerrainPlane(1000);
    linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
    linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
    linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));
    playerShapes.put("dolphin", new ImportedModel("dolphinHighPoly.obj"));
    playerShapes.put("guy", new ImportedModel("avatar.obj"));
    playerShapes.put("squareGuy", new ImportedModel("square-avatar.obj"));
    playerShapes.put("bucket", new ImportedModel("cylinder.obj"));
  }

  
  @Override
  public void loadSounds() { 
    AudioResource resource1, resource2;
    audioMgr = engine.getAudioManager();
    resource1 = audioMgr.createAudioResource("assets/sounds/bad-game-music.wav", AudioResourceType.AUDIO_SAMPLE);
    bgSound = new Sound(resource1, SoundType.SOUND_MUSIC, 0, true);
    bgSound.initialize(audioMgr);
    bgSound.setMaxDistance(10.0f);
    bgSound.setMinDistance(0.5f);
    bgSound.setRollOff(5.0f);

    resource2 = audioMgr.createAudioResource("assets/sounds/click.wav", AudioResourceType.AUDIO_SAMPLE);
    ghostSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
    ghostSound.initialize(audioMgr);
    ghostSound.setMaxDistance(50.0f);
    ghostSound.setMinDistance(10.0f);
    ghostSound.setRollOff(2.0f);
  }

  @Override
  public void loadTextures() {
    playerTextures = new HashMap<>();
    playerTextures.put("dolphin_normal", new TextureImage("Dolphin_HighPolyUV.png"));
    playerTextures.put("dolphin_red", new TextureImage("redDolphin.jpg"));
    playerTextures.put("bucket", new TextureImage("Cylinder.png"));
    playerTextures.put("guy", new TextureImage("guy_2.png"));
    playerTextures.put("squareGuy", new TextureImage("avatar-tex.png"));
    hills = new TextureImage("hills.jpg");
    brick = new TextureImage("brick1.jpg");
  }

  @Override
  public void loadSkyBoxes() {
    sky = (engine.getSceneGraph()).loadCubeMap("sky");
    (engine.getSceneGraph()).setActiveSkyBoxTexture(sky);
    (engine.getSceneGraph()).setSkyBoxEnabled(true);
  }

  @Override
  public void buildObjects() {
    Matrix4f initialTranslation, initialRotation, initialScale;

    // build avatar
    avatar = new GameObject(GameObject.root(), playerShapes.get(avatarName), playerTextures.get(textureName));
    initialTranslation = (new Matrix4f()).translation(-1f, 0f, 1f);
    avatar.setLocalTranslation(initialTranslation);
    initialRotation = (new Matrix4f()).rotationY((float) java.lang.Math.toRadians(135.0f));
    avatar.setLocalRotation(initialRotation);
    initialScale = (new Matrix4f()).scaling(0.25f);
    avatar.setLocalScale(initialScale);

    // build terrain object
    terr = new GameObject(GameObject.root(), terrS, brick);
    initialTranslation = (new Matrix4f()).translation(0f, 0f, 0f);
    terr.setLocalTranslation(initialTranslation);
    initialScale = (new Matrix4f()).scaling(200.0f, 5.0f, 200.0f);
    terr.setLocalScale(initialScale);
    terr.setHeightMap(hills);
    // set tiling for terrain texture
    terr.getRenderStates().setTiling(1);
    terr.getRenderStates().setTileFactor(200);

    // add X,Y,Z axes
    x = new GameObject(GameObject.root(), linxS);
    y = new GameObject(GameObject.root(), linyS);
    z = new GameObject(GameObject.root(), linzS);
    (x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
    (y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
    (z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));
  }

  private void setupMainCamera() {
    cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
    camCtrl = new CameraOrbit3D(cam, avatar, engine);
    camCtrl.setElevationMin(2f);
    camCtrl.setRadius(5f);
  }

  @Override
  public void initializeLights() {
    Light.setGlobalAmbient(.5f, .5f, .5f);

    light = new Light();
    light.setLocation(new Vector3f(0f, 5f, 0f));
    (engine.getSceneGraph()).addLight(light);
  }

  @Override
  public void initializeGame() {
    (engine.getRenderSystem()).setWindowDimensions(1900, 1000);
    setupMainCamera();
    lastFrameTime = System.currentTimeMillis();
    currFrameTime = System.currentTimeMillis();

    im = engine.getInputManager();

    FwdAction fwdAction = new FwdAction(this);
    TurnAction turnAction = new TurnAction(this);

    im.associateActionWithAllGamepads(
        net.java.games.input.Component.Identifier.Button._1,
        fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.W,
        fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.S,
        fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.D,
        turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.A,
        turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllGamepads(
        net.java.games.input.Component.Identifier.Axis.X,
        turnAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    setupNetworking();

    physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
    float mass = 1.0f;
    float up[] = { 0, 1, 0 };
    float radius = 0.75f;
    float height = 1.0f;
    double[] tempTransform;

    // add avatar object
    Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
    tempTransform = toDoubleArray(translation.get(vals));
    avatarP = (engine.getSceneGraph()).addPhysicsCapsuleX(
        mass, tempTransform, radius, height);
    avatarP.setBounciness(1.0f);
    avatarP.setDamping(0.8f, 0.8f);
    avatarP.setSleepThresholds(0.05f, 0.05f); // Low sleep thresholds
    avatar.setPhysicsObject(avatarP);

    engine.enableGraphicsWorldRender();
    engine.enablePhysicsWorldRender();

    bgSound.play();
  }

  public float[] toFloatArray(double[] arr) {
    if (arr == null)
      return null;
    int n = arr.length;
    float[] ret = new float[n];
    for (int i = 0; i < n; i++) {
      ret[i] = (float) arr[i];
    }
    return ret;
  }

  public double[] toDoubleArray(float[] arr) {
    if (arr == null)
      return null;
    int n = arr.length;
    double[] ret = new double[n];
    for (int i = 0; i < n; i++) {
      ret[i] = (double) arr[i];
    }
    return ret;
  }

  public GameObject getAvatar() {
    return avatar;
  }

  public ObjShape getShape(String shape) {
    return playerShapes.get(shape);
  }

  public TextureImage getTexture(String texture) {
    return playerTextures.get(texture);
  }

  public void setTimes() {
    lastFrameTime = currFrameTime;
    currFrameTime = System.currentTimeMillis();
    deltaTime = (currFrameTime - lastFrameTime) / 1000.0;
    elapsedTime += deltaTime;
  }

  public double getElapsedTime() {
    return elapsedTime;
  }

  public Sound getGhostSound() {
    return ghostSound;
  }

  @Override
  public void update() {
    setTimes();
    drawHUD();
    setEarParameters();
    im.update((float) deltaTime);
    camCtrl.updateCameraPosition();
    setGroundHeightForAvatar();
    updatePhysics();
    protClient.sendMoveMessage(avatar.getWorldLocation());
    processNetworking((float) elapsedTime);
  }

  public void drawHUD() {
    String dispStr2 = "camera position = "
        + (cam.getLocation()).x()
        + ", " + (cam.getLocation()).y()
        + ", " + (cam.getLocation()).z();
    Vector3f hud2Color = new Vector3f(1, 1, 1);
    (engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);
  }


  public void setEarParameters() { 
    audioMgr.getEar().setLocation(avatar.getWorldLocation());
    audioMgr.getEar().setOrientation(cam.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
  }

  public void setGroundHeightForAvatar() {
    Vector3f loc = avatar.getWorldLocation();
    float height = terr.getHeight(loc.x(), loc.z()) + 1.2f; 
    Matrix4f currentTransform = new Matrix4f(avatar.getWorldRotation());
    currentTransform.setTranslation(loc.x(), height, loc.z());
    double[] transformArray = toDoubleArray(currentTransform.get(new float[16]));
    avatar.setLocalTranslation(currentTransform);
    avatar.getPhysicsObject().setTransform(transformArray);
  }

  public GameObject getTerr() {
    return terr;
  }

  public void updatePhysics() {
    AxisAngle4f aa = new AxisAngle4f();
    Matrix4f mat = new Matrix4f();
    Matrix4f mat2 = new Matrix4f().identity();
    Matrix4f mat3 = new Matrix4f().identity();

    checkForCollisions();
    physicsEngine.update((float) elapsedTime);
    for (GameObject go : engine.getSceneGraph().getGameObjects()) {
      if (go.getPhysicsObject() != null) {
        mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
        mat2.set(3, 0, mat.m30());
        mat2.set(3, 1, mat.m31());
        mat2.set(3, 2, mat.m32());
        go.setLocalTranslation(mat2);
        // set rotation
        // mat.getRotation(aa);
        // mat3.rotation(aa);
        // go.setLocalRotation(mat3);
      }
    }
  }

  private void checkForCollisions() {
    com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
    com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
    com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
    com.bulletphysics.dynamics.RigidBody object1, object2;
    com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
    dynamicsWorld = ((JBulletPhysicsEngine) physicsEngine).getDynamicsWorld();
    dispatcher = dynamicsWorld.getDispatcher();
    int manifoldCount = dispatcher.getNumManifolds();
    for (int i = 0; i < manifoldCount; i++) {
      manifold = dispatcher.getManifoldByIndexInternal(i);
      object1 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody0();
      object2 = (com.bulletphysics.dynamics.RigidBody) manifold.getBody1();
      JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
      JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
      for (int j = 0; j < manifold.getNumContacts(); j++) {
        contactPoint = manifold.getContactPoint(j);
        if (contactPoint.getDistance() < 0.0f) {
          System.out.println("---- hit between " + obj1 + " and " + obj2);
          break;
        }
      }
    }
  }

  public ProtocolClient getProtClient() {
    return protClient;
  }

  // @Override
  // public void keyPressed(KeyEvent e) {
  // switch (e.getKeyCode()) {
  // case KeyEvent.VK_W: {
  // Vector3f oldPosition = avatar.getWorldLocation();
  // Vector4f fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
  // fwdDirection.mul(avatar.getWorldRotation());
  // fwdDirection.mul(0.55f);
  // Vector3f newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(),
  // fwdDirection.z());
  // avatar.setLocalLocation(newPosition);
  // protClient.sendMoveMessage(avatar.getWorldLocation());
  // break;
  // }
  // case KeyEvent.VK_D: {
  // Matrix4f oldRotation = new Matrix4f(avatar.getWorldRotation());
  // Vector4f oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRotation);
  // Matrix4f rotAroundAvatarUp = new Matrix4f().rotation(-.05f, new
  // Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
  // Matrix4f newRotation = oldRotation;
  // newRotation.mul(rotAroundAvatarUp);
  // avatar.setLocalRotation(newRotation);
  // break;
  // }
  // }
  // super.keyPressed(e);
  // }

  // ---------- NETWORKING SECTION ----------------

  public GhostManager getGhostManager() {
    return gm;
  }

  public Engine getEngine() {
    return engine;
  }

  private void setupNetworking() {
    isClientConnected = false;
    try {
      protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (protClient == null) {
      System.out.println("missing protocol host");
    } else { // Send the initial join message with a unique identifier for this client
      System.out.println("sending join message to protocol host");
      protClient.sendJoinMessage();
    }
  }

  protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
    if (protClient != null)
      protClient.processPackets();
  }

  public Vector3f getPlayerPosition() {
    return avatar.getWorldLocation();
  }

  public String getAvatarName() {
    return avatarName;
  }

  public String getTextureName() {
    return textureName;
  }

  public void setIsConnected(boolean value) {
    this.isClientConnected = value;
  }

  private class SendCloseConnectionPacketAction extends AbstractInputAction {
    @Override
    public void performAction(float time, net.java.games.input.Event evt) {
      if (protClient != null && isClientConnected == true) {
        protClient.sendByeMessage();
      }
    }
  }
}

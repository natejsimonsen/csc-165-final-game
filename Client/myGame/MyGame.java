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
import java.util.Random;

import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.audio.*;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame {
  private static Engine engine;
  private InputManager im;
  private GhostManager gm;
  private String avatarName;
  private String textureName;
  private PhysicsEngine physicsEngine;
  private boolean animatedShape = false;
  private boolean lightEnabled = false;

  private int counter = 0;
  private int avatarScore = 0;
  private int ghostScore = 0;
  private Vector3f currentPosition;
  private Matrix4f initialTranslation, initialRotation, initialScale;
  private double lastFrameTime, currFrameTime, deltaTime, elapsedTime = 0;

  private PhysicsObject terrP, avatarP;
  private GameObject terr, avatar, x, y, z, bucket;
  private ArrayList<GameObject> foods;
  private ObjShape terrS, linxS, linyS, linzS, cubeS, cylinderS;
  private TextureImage foodTex, hills, brick, bucketTex;
  private Light light, spotLight;
  private HashMap<String, TextureImage> playerTextures;
  private HashMap<String, ObjShape> playerShapes;
  private CameraOrbit3D camCtrl;
  private Camera cam;
  private IAudioManager audioMgr;
  private Sound bgSound, ghostSound, foodSound;
  private Random random;

  private String serverAddress;
  private int serverPort;
  private ProtocolType serverProtocol;
  private ProtocolClient protClient;
  private boolean isClientConnected = false;
  private int sky;
  private float vals[] = new float[16];

  public MyGame(String serverAddress, int serverPort, String protocol, String avatarName, String textureName) {
    super();
    gm = new GhostManager(this);
    this.serverAddress = serverAddress;
    this.serverPort = serverPort;
    this.serverProtocol = ProtocolType.UDP;
    this.avatarName = avatarName;
    this.textureName = textureName;
    animatedShape = avatarName.equals("squareGuy");
    random = new Random();
    foods = new ArrayList<GameObject>();
  }

  public static void main(String[] args) {
    try {
      File inputFile = new File("config.txt");
      Scanner fileScanner = new Scanner(inputFile);
      String line = fileScanner.nextLine();
      String[] data = line.split("/");
      String av = data[0];
      String tex = data[1];
      MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2], av, tex);
      engine = new Engine(game);
      game.initializeSystem();
      game.game_loop();
    } catch (FileNotFoundException e) {
      System.err.println("Cannot find 'config.txt' file");
    }
  }

  @Override
  public void loadShapes() {
    playerShapes = new HashMap<>();
    terrS = new TerrainPlane(1000);
    linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
    linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
    linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, -3f));
    cubeS = new Cube();
    cylinderS = new ImportedModel("cylinder.obj");
    playerShapes.put("dolphin", new ImportedModel("dolphinHighPoly.obj"));
    playerShapes.put("guy", new ImportedModel("avatar.obj"));
    AnimatedShape animAvatar = new AnimatedShape("avatar.rkm", "avatar.rks");
    animAvatar.loadAnimation("WALK", "walk.rka");
    playerShapes.put("squareGuy", animAvatar);
    playerShapes.put("bucket", new ImportedModel("cylinder.obj"));
  }

  private float getRelativeFoodDistance(Vector3f possibleLocation) {
    float min = 10000000000f;
    for (int i = 0; i < foods.size(); i++) {
      Vector3f foodLocation = foods.get(i).getWorldLocation();
      float distance = (float) foodLocation.distance(possibleLocation);
      if (distance < min)
        min = distance;
    }
    return min;
  }

  public void playGhostFoodSound() {
    foodSound.setLocation(gm.getGhost().getWorldLocation());
    foodSound.play();
  }

  private void placeRandomFood() {
    for (int i = 0; i < 20; i++) {
      GameObject food = new GameObject(GameObject.root(), cubeS, foodTex);
      Vector3f location = randomFoodLocation();
      while (getRelativeFoodDistance(location) < 10f) {
        location = randomFoodLocation();
      }
      food.setLocalLocation(randomFoodLocation());
      Matrix4f initialScale = (new Matrix4f()).scaling(0.25f);
      food.setLocalScale(initialScale);
      foods.add(food);
    }
  }

  private void checkFoodEaten() {
    GameObject foodToRemove = null;
    for (int i = 0; i < foods.size(); i++) {
      GameObject food = foods.get(i);
      Vector3f foodLocation = food.getWorldLocation();
      Vector3f avatarLocation = avatar.getWorldLocation();
      if (foodLocation.distance(avatarLocation) < 1.75f) {
        foodToRemove = food;
        engine.getSceneGraph().removeGameObject(food);
        foodSound.setLocation(avatar.getWorldLocation());
        foodSound.play();
        avatarScore++;
        protClient.sendUpdateScoreMessage();
      }
    }
    if (foodToRemove != null)
      foods.remove(foodToRemove);
  }

  private Vector3f randomFoodLocation() {
    int range = 100;
    float x = random.nextFloat() * range - (range / 2);
    float z = random.nextFloat() * range - (range / 2);
    float y = terr.getHeight(x, z) + 1.2f;
    return new Vector3f(x, y, z);
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
    foodTex = new TextureImage("food.png");
    bucketTex = new TextureImage("Cylinder.png");
  }

  @Override
  public void loadSounds() {
    AudioResource resource1, resource2, resource3;
    audioMgr = engine.getAudioManager();

    resource1 = audioMgr.createAudioResource("assets/sounds/bad-game-music.wav", AudioResourceType.AUDIO_SAMPLE);
    bgSound = new Sound(resource1, SoundType.SOUND_MUSIC, 40, true);
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

    resource3 = audioMgr.createAudioResource("assets/sounds/powerUp.wav", AudioResourceType.AUDIO_SAMPLE);
    foodSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
    foodSound.initialize(audioMgr);
    foodSound.setMaxDistance(50.0f);
    foodSound.setMinDistance(10.0f);
    foodSound.setRollOff(2.0f);
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

    // build bucket
    bucket = new GameObject(avatar, cylinderS, bucketTex);
    bucket.setLocalLocation(new Vector3f(-0.5f, 1.25f, 0.75f));
    bucket.propagateRotation(true);
    bucket.propagateTranslation(true);
    bucket.applyParentRotationToPosition(true);

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

    spotLight = new Light();
    spotLight.setType(Light.LightType.SPOTLIGHT);
    spotLight.setLocation(new Vector3f(3f, 2f, 4f));
    spotLight.setDirection(new Vector3f(1f, 1f, 1f));
    (engine.getSceneGraph()).addLight(spotLight);
  }

  public Light getSpotLight() {
    return spotLight;
  }

  public boolean getIsLightEnabled() {
    return lightEnabled;
  }

  public void toggleLightEnabled() {
    lightEnabled = !lightEnabled;
  }

  private void setActions() {
    FwdAction fwdAction = new FwdAction(this);
    TurnAction turnAction = new TurnAction(this);
    ToggleLightAction toggleLightAction = new ToggleLightAction(this);

    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.W,
        fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllKeyboards(
        net.java.games.input.Component.Identifier.Key.S,
        fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    im.associateActionWithAllGamepads(
        net.java.games.input.Component.Identifier.Axis.Y,
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

    im.associateActionWithAllKeyboards(
      net.java.games.input.Component.Identifier.Key.L,
      toggleLightAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    im.associateActionWithAllGamepads(
        net.java.games.input.Component.Identifier.Button._1,
        toggleLightAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
  }

  private void setupPhysicsObjects() {
    physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
    float mass = 0.01f;
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
    avatarP.setSleepThresholds(0.05f, 0.05f);
    avatar.setPhysicsObject(avatarP);
  }

  @Override
  public void initializeGame() {
    setupNetworking();
    (engine.getRenderSystem()).setWindowDimensions(1900, 1000);
    setupMainCamera();
    lastFrameTime = System.currentTimeMillis();
    currFrameTime = System.currentTimeMillis();
    im = engine.getInputManager();
    setActions();
    setupPhysicsObjects();
    engine.enableGraphicsWorldRender();
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
    if (animatedShape == true)
      ((AnimatedShape) avatar.getShape()).updateAnimation();
    im.update((float) deltaTime);
    camCtrl.updateCameraPosition();
    setGroundHeightForAvatar();
    updatePhysics();
    if (foods.size() < 2)
      placeRandomFood();
    checkFoodEaten();
    protClient.sendMoveMessage(avatar.getWorldLocation());
    processNetworking((float) elapsedTime);
  }

  public void drawHUD() {
    String dispStr2 = "Score: " + avatarScore + ", Other Player Score: " + ghostScore;
    Vector3f hud2Color = new Vector3f(1, 1, 1);
    (engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 20);
  }

  public void updateGhostScore() {
    ghostScore++;
  }

  public void setEarParameters() {
    audioMgr.getEar().setLocation(avatar.getWorldLocation());
    audioMgr.getEar().setOrientation(cam.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
  }

  public void setGroundHeightForAvatar() {
    Vector3f loc = avatar.getWorldLocation();
    float height = terr.getHeight(loc.x(), loc.z());
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

  private void exit() {
    protClient.sendByeMessage();
    System.exit(0);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        exit();
        break;
    }

    if (animatedShape == false)
      return;
    AnimatedShape s = (AnimatedShape) avatar.getShape();
    switch (e.getKeyCode()) {
      case KeyEvent.VK_Z:
        s.stopAnimation();
        s.playAnimation("WALK", 0.5f, AnimatedShape.EndType.LOOP, 0);
        break;
      case KeyEvent.VK_Q:
        s.stopAnimation();
        break;
    }

    super.keyPressed(e);
  }

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

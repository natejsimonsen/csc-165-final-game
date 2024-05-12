package tage;

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
    private Camera camera;
    private GameObject avatar;
    private float cameraAzimuth; // Horizontal angle around the target
    private float cameraElevation; // Vertical angle above the target
    private float cameraRadius; // Distance from the camera to the target
    private float elevationMin = -89f; // Minimum elevation angle
    private float elevationMax = 89f; // Maximum elevation angle

    /**
     * Constructs a CameraOrbit3D object to manage the orbiting camera behavior
     * around a specified avatar within the game.
     *
     * @param cam   The camera that will orbit around the avatar.
     * @param av    The avatar GameObject around which the camera will orbit.
     * @param e     The game engine instance to which this camera belongs.
     */
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

    /**
     * Sets up input actions for camera control, including azimuth rotation,
     * elevation adjustment, and radius (distance) change.
     */
    private void setupInputs() {
        InputManager im = engine.getInputManager();

        // Actions for orbit azimuth control
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction(0);
        OrbitAzimuthAction azmActionLeft = new OrbitAzimuthAction(-1);
        OrbitAzimuthAction azmActionRight = new OrbitAzimuthAction(1);
        im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LEFT, azmActionLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.RIGHT, azmActionRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // Actions for orbit radius control
        OrbitRadiusAction radiusActionOut = new OrbitRadiusAction(1);
        OrbitRadiusAction radiusActionIn = new OrbitRadiusAction(-1);
        im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._0, radiusActionIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._1, radiusActionOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.N, radiusActionIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.M, radiusActionOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        // Actions for orbit elevation control
        OrbitElevationAction elevationAction = new OrbitElevationAction(0);
        OrbitElevationAction elevationActionUp = new OrbitElevationAction(1);
        OrbitElevationAction elevationActionDown = new OrbitElevationAction(-1);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, elevationActionUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, elevationActionDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RY, elevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    /**
     * Updates the camera's position based on its current azimuth, elevation,
     * and distance from the target avatar. This method calculates the camera's
     * position in spherical coordinates relative to the avatar and then
     * converts those coordinates to Cartesian coordinates for positioning.
     */
    public void updateCameraPosition() {
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math.toDegrees((double) avatarRot.angleSigned(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0)));
        float totalAz = cameraAzimuth - (float) avatarAngle;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);
        float x = cameraRadius * (float) (Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float) (Math.sin(phi));
        float z = cameraRadius * (float) (Math.cos(phi) * Math.cos(theta));
        camera.setLocation(new Vector3f(x, y, z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    /**
     * Sets the minimum allowed elevation angle for the camera.
     * @param min The minimum elevation angle in degrees.
     */
    public void setElevationMin(float min) {
        elevationMin = min;
    }

    /**
     * Sets the maximum allowed elevation angle for the camera.
     * @param max The maximum elevation angle in degrees.
     */
    public void setElevationMax(float max) {
        elevationMax = max;
    }

    /**
     * Adjusts the camera's azimuth angle relative to the current position.
     * @param amount The amount to adjust the azimuth angle by, in degrees.
     */
    public void setRelativeAzimuth(float amount) {
        cameraAzimuth += amount;
        updateCameraPosition();
    }

    /**
     * Sets the camera's elevation angle directly.
     * @param amount The new elevation angle, in degrees.
     */
    public void setElevation(float amount) {
        cameraElevation = amount;
        updateCameraPosition();
    }

    /**
     * Sets the camera's distance from the target avatar.
     * @param amount The new distance to set.
     */
    public void setRadius(float amount) {
        cameraRadius = amount;
        updateCameraPosition();
    }

    // Inner classes for input actions follow here
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

            if (direction == 1 || direction == -1) elevationAmount = speed * direction;

            float newCameraElevation = cameraElevation + elevationAmount;
            if (newCameraElevation > elevationMax || newCameraElevation < elevationMin) elevationAmount = 0.0f;

            cameraElevation += elevationAmount;
            updateCameraPosition();
        }
    }
}

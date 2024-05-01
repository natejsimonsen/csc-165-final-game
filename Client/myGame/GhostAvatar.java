package myGame;

import java.util.UUID;

import tage.*;
import org.joml.*;

public class GhostAvatar extends GameObject {
	UUID uuid;
	float[] vals;

	public GhostAvatar(UUID id, ObjShape s, TextureImage t, Vector3f p) {	
		super(GameObject.root(), s, t);
		uuid = id;
		vals = new float[16];
		setPosition(p);
	}
	
	public UUID getID() { 
		return uuid; 
	}

	public void setPosition(Vector3f m) { 
		setLocalLocation(m); 
		Matrix4f wl = getWorldTranslation();
		if (getPhysicsObject() != null)
			getPhysicsObject().setTransform(toDoubleArray(wl.get(vals)));
	}

	public Vector3f getPosition() { 
		return getWorldLocation(); 
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
}

package net.sf.hale.particle;

import de.matthiasmann.twl.Color;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.Saveable;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONObject;

public abstract class AnimationBase implements Saveable {
	
	private final int halfWidth;
	private final int halfHeight;
	
	private float r, g, b, a;
	private float vr, vg, vb, va;
	
	private float r2, g2, b2;
	private float vr2, vg2, vb2;
	
	private float positionX, positionY;
	private float velocityX, velocityY;
	private float speed;
	private float velocityAngle;
	
	private float rotation;
	private float rotationSpeed;
	
	private float secondsRemaining;
	
	@Override public JSONOrderedObject save() {
		JSONOrderedObject data = new JSONOrderedObject();
		
		// use java.awt.color which is more robust than the TWL color and can
		// convert floats RGBA to a color integer
		data.put( "color", getColorString(new java.awt.Color(r, g, b, a)) );
		
		data.put( "colorVelocity", getColorString(new java.awt.Color(vr, vg, vb, va)) );
		
		data.put( "secondaryColor", getColorString(new java.awt.Color(r2, g2, b2)) );
		
		data.put( "secondaryColorVelocity", getColorString(new java.awt.Color(vr2, vg2, vb2)) );
		
		data.put("positionX", positionX);
		data.put("positionY", positionY);
		data.put("velocityX", velocityX);
		data.put("velocityY", velocityY);
		
		data.put("rotation", rotation);
		data.put("rotationSpeed", rotationSpeed);
		
		data.put("secondsRemaining", secondsRemaining);
		
		return data;
	}
	
	protected final void loadBase(SimpleJSONObject data) {
		secondsRemaining = data.get("secondsRemaining", 0.0f);
		rotationSpeed = data.get("rotationSpeed", 0.0f);
		rotation = data.get("rotation", 0.0f);
		
		velocityY = data.get("velocityY", 0.0f);
		velocityX = data.get("velocityX", 0.0f);
		positionX = data.get("positionX", 0.0f);
		positionY = data.get("positionY", 0.0f);
		
		String colorString = data.get("color", null);
		Color color = Color.parserColor(colorString);
		r = color.getRedFloat();
		g = color.getGreenFloat();
		b = color.getBlueFloat();
		a = color.getAlphaFloat();
		
		String colorVString = data.get("colorVelocity", null);
		Color colorV = Color.parserColor(colorVString);
		vr = colorV.getRedFloat();
		vg = colorV.getGreenFloat();
		vb = colorV.getBlueFloat();
		va = colorV.getAlphaFloat();
		
		String color2String = data.get("secondaryColor", null);
		Color color2 = Color.parserColor(color2String);
		r2 = color2.getRedFloat();
		g2 = color2.getGreenFloat();
		b2 = color2.getBlueFloat();
		
		String color2VString = data.get("secondaryColorVelocity", null);
		Color color2V = Color.parserColor(color2VString);
		vr2 = color2V.getRedFloat();
		vg2 = color2V.getGreenFloat();
		vb2 = color2V.getBlueFloat();
	}
	
	private String getColorString(java.awt.Color color) {
		return '#' + String.format("%08x", color.getRGB());
	}
	
	public AnimationBase(int halfWidth, int halfHeight) {
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
        rotation = 0.0f;
	}
	
	public AnimationBase(AnimationBase other) {
        halfWidth = other.halfWidth;
        halfHeight = other.halfHeight;

        r = other.r;
        g = other.g;
        b = other.b;
        a = other.a;
        vr = other.vr;
        vg = other.vg;
        vb = other.vb;
        va = other.va;

        r2 = other.r2;
        g2 = other.g2;
        b2 = other.b2;
        vr2 = other.vr2;
        vg2 = other.vg2;
        vb2 = other.vb2;

        positionX = other.positionX;
        positionY = other.positionY;

        velocityX = other.velocityX;
        velocityY = other.velocityY;

        speed = other.speed;
        velocityAngle = other.velocityAngle;

        secondsRemaining = other.secondsRemaining;
	}
	
	public boolean elapseTime(float seconds) {
		rotation += (rotationSpeed * seconds);
		
		positionX += (velocityX * seconds);
		positionY += (velocityY * seconds);
		
		r += vr * seconds;
		g += vg * seconds;
		b += vb * seconds;
		a += va * seconds;
		
		r2 += vr2 * seconds;
		g2 += vg2 * seconds;
		b2 += vb2 * seconds;
		
		secondsRemaining -= seconds;
		
		return secondsRemaining <= 0.0f;
	}
	
	public final void setRotation(float angle) {
        rotation = angle;
	}
	
	public final void setRotationSpeed(float angle) {
        rotationSpeed = angle;
	}
	
	public final void offsetPosition(float x, float y) {
        positionX += x;
        positionY += y;
	}
	
	public final void setPosition(Point screenPoint) {
        positionX = screenPoint.x;
        positionY = screenPoint.y;
	}
	
	public final void setPosition(float x, float y) {
        positionX = x;
        positionY = y;
	}
	
	public final void setSecondaryRed(float r) {
        r2 = r;
	}
	
	public final void setSecondaryGreen(float g) {
        g2 = g;
	}
	
	public final void setSecondaryBlue(float b) {
        b2 = b;
	}
	
	public final void setSecondaryRedSpeed(float vr) {
        vr2 = vr;
	}
	
	public final void setSecondaryGreenSpeed(float vg) {
        vg2 = vg;
	}
	
	public final void setSecondaryBlueSpeed(float vb) {
        vb2 = vb;
	}
	
	public final void setRed(float r) {
		this.r = r;
	}
	
	public final void setGreen(float g) {
		this.g = g;
	}
	
	public final void setBlue(float b) {
		this.b = b;
	}
	
	public final void setAlpha(float a) {
		this.a = a;
	}
	
	public final void setAlphaSpeed(float va) {
		this.va = va;
	}
	
	public final void setRedSpeed(float vr) {
		this.vr = vr;
	}
	
	public final void setGreenSpeed(float vg) {
		this.vg = vg;
	}
	
	public final void setBlueSpeed(float vb) {
		this.vb = vb;
	}
	
	public void setDuration(float lifetime) {
        secondsRemaining = lifetime;
	}
	
	public final void setVelocity(float[] vector) {
        velocityX = vector[0];
        velocityY = vector[1];
        speed = vector[2];
        velocityAngle = vector[3];
	}
	
	public final void setVelocity(float vx, float vy) {
        velocityX = vx;
        velocityY = vy;
        speed = (float)Math.sqrt(vx * vx + vy * vy);
        velocityAngle = (float)Math.atan(vy / vx);
	}
	
	public final void setVelocityMagnitudeAngle(float magnitude, float angle) {
        speed = magnitude;
        velocityAngle = angle;
        velocityX = (float)Math.cos(angle) * magnitude;
        velocityY = (float)Math.sin(angle) * magnitude;
		
	}
	
	public void finish() {
        secondsRemaining = 0.0f;
	}
	
	public final int getHalfWidth() { return halfWidth; }
	public final int getHalfHeight() { return halfHeight; }
	public final float getR() { return r; }
	public final float getG() { return g; }
	public final float getB() { return b; }
	public final float getA() { return a; }
	
	public final float getR2() { return r2; }
	public final float getG2() { return g2; }
	public final float getB2() { return b2; }
	
	public final float getX() { return positionX; }
	public final float getY() { return positionY; }
	
	public final float getVX() { return velocityX; }
	public final float getVY() { return velocityY; }
	public final float getSpeed() { return speed; }
	public final float getVelocityAngle() { return velocityAngle; }
	
	public final float getSecondsRemaining() { return secondsRemaining; }
	
	public final float getRotation() { return rotation; }
	public final float getRotationSpeed() { return rotationSpeed; }
}

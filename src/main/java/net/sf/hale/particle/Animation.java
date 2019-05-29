/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.particle;

import de.matthiasmann.twl.Color;
import net.sf.hale.icon.Icon;
import net.sf.hale.icon.SimpleIcon;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// TODO support animations replacing / modifying sub icons & icons on the target creature

public class Animation extends AnimationBase implements Animated {
    private final List<AnimationFrame> frames;
    private final int width, height;
    private final String textureSprite;
    private final int halfWidth, halfHeight;
    private DrawingMode drawingMode;
    private float frameDuration;
    private int currentFrameIndex;
    private int numLoops;
    private int texture;

    public Animation(Animation other) {
        super(other);

        width = other.width;
        height = other.height;
        halfWidth = other.halfWidth;
        halfHeight = other.halfHeight;
        texture = other.texture;
        textureSprite = other.textureSprite;

        frameDuration = other.frameDuration;

        frames = new ArrayList<>();
        for (AnimationFrame frame : other.frames) {
            frames.add(new AnimationFrame(frame));
        }

        drawingMode = other.drawingMode;
    }

    public Animation(String sprite, float duration) {
        this(SpriteManager.getSprite(sprite), duration, sprite);
    }

    public Animation(String sprite) {
        this(SpriteManager.getSprite(sprite), 0.0f, sprite);
    }

    public Animation(SimpleIcon icon) {
        this(SpriteManager.getSprite(icon.getSpriteID()), 0.0f, icon.getSpriteID());
        setColor(icon.getColor());
    }

    public Animation(Sprite sprite, float defaultFrameDuration, String textureSprite) {
        super(sprite.getWidth() / 2, sprite.getHeight() / 2);

        drawingMode = DrawingMode.AboveEntities;

        this.textureSprite = textureSprite;
        texture = sprite.getTextureReference();
        width = sprite.getWidth();
        height = sprite.getHeight();
        halfWidth = width / 2;
        halfHeight = height / 2;

        frameDuration = defaultFrameDuration;
        frames = new ArrayList<>();

        frames.add(new AnimationFrame(sprite.getTexCoordStartX(), sprite.getTexCoordStartY(),
                sprite.getTexCoordEndX(), sprite.getTexCoordEndY(), frameDuration));

        setRed(1.0f);
        setBlue(1.0f);
        setGreen(1.0f);
        setAlpha(1.0f);

        setDuration(frameDuration);
    }

    public static Animated load(SimpleJSONObject data) throws LoadGameException {
        float frameDuration = data.get("frameDuration", 0.0f);
        String sprite = data.get("texture", null);

        Animation animation = new Animation(sprite, frameDuration);
        animation.frames.clear();

        // do the superclass loading
        animation.loadBase(data);

        animation.drawingMode = DrawingMode.valueOf(data.get("drawingMode", null));
        animation.currentFrameIndex = data.get("currentFrame", 0);
        animation.numLoops = data.get("numLoops", 0);

        for (SimpleJSONArrayEntry entry : data.getArray("frames")) {
            SimpleJSONObject entryData = entry.getObject();

            float initialDuration = entryData.get("initialDuration", 0.0f);
            float texCoordStartX = entryData.get("texCoordStartX", 0.0f);
            float texCoordStartY = entryData.get("texCoordStartY", 0.0f);
            float texCoordEndX = entryData.get("texCoordEndX", 0.0f);
            float texCoordEndY = entryData.get("texCoordEndY", 0.0f);

            AnimationFrame frame = new AnimationFrame(texCoordStartX, texCoordStartY,
                    texCoordEndX, texCoordEndY, initialDuration);
            frame.duration = entryData.get("duration", 0.0f);

            animation.frames.add(frame);
        }

        return animation;
    }

    @Override
    public JSONOrderedObject save() {
        JSONOrderedObject data = super.save();

        data.put("class", getClass().getName());
        data.put("texture", textureSprite);
        data.put("frameDuration", frameDuration);

        data.put("drawingMode", drawingMode.toString());
        data.put("currentFrame", currentFrameIndex);
        data.put("numLoops", numLoops);

        JSONOrderedObject[] framesData = new JSONOrderedObject[frames.size()];
        int i = 0;
        for (AnimationFrame frame : frames) {
            framesData[i] = new JSONOrderedObject();

            framesData[i].put("initialDuration", frame.initialDuration);
            framesData[i].put("duration", frame.duration);
            framesData[i].put("texCoordStartX", frame.texCoordStartX);
            framesData[i].put("texCoordStartY", frame.texCoordStartY);
            framesData[i].put("texCoordEndX", frame.texCoordEndX);
            framesData[i].put("texCoordEndY", frame.texCoordEndY);

            i++;
        }
        data.put("frames", framesData);

        return data;
    }

    @Override
    public void cacheSprite() {
        Sprite sprite = SpriteManager.getSprite(textureSprite);
        texture = sprite.getTextureReference();
    }

    public void setLoopInfinite() {
        numLoops = Integer.MAX_VALUE;
    }

    public float getLoopLength() {
        float totalDuration = 0.0f;
        for (AnimationFrame frame : frames) {
            totalDuration += frame.duration;
        }

        return totalDuration;
    }

    @Override
    public DrawingMode getDrawingMode() {
        return drawingMode;
    }

    public void setDrawingMode(String drawingMode) {
        this.drawingMode = DrawingMode.valueOf(drawingMode);
    }

    @Override
    public void setDuration(float duration) {
        super.setDuration(duration);

        float loops = duration / getLoopLength();
        numLoops = (int) loops + 1;
    }

    @Override
    public void setDurationInfinite() {
        super.setDuration(Float.MAX_VALUE);

        frameDuration = Float.MAX_VALUE;

        // if there is only one frame, set its duration to infinite
        if (frames.size() == 1) {
            frames.get(0).duration = Float.MAX_VALUE;
        } else {
            // otherwise, loop endlessly
            numLoops = Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean initialize() {
        if (getSecondsRemaining() == 0.0f) {
            super.setDuration(getLoopLength() * numLoops);
        }

        return true;
    }

    @Override
    public boolean isDrawable() {
        return !frames.isEmpty();
    }

    public void clearFrames() {
        frames.clear();
    }

    // add a frame with the specified icon sprite and color, if possible
    public void addFrameAndSetColor(Icon icon) {
        if (!(icon instanceof SimpleIcon)) return;

        SimpleIcon simpleIcon = (SimpleIcon) icon;

        addFrame(simpleIcon.getSpriteID());
        setColor(simpleIcon.getColor());
    }

    // add a frame with the default frame duration
    public void addFrame(String sprite) {
        if (sprite == null) return;

        addFrame(SpriteManager.getSprite(sprite), frameDuration);
    }

    public void addFrame(String sprite, float duration) {
        if (sprite == null) return;

        addFrame(SpriteManager.getSprite(sprite), duration);
    }

    // add a frame with a specified frame duration
    public void addFrame(Sprite sprite, float duration) {
        frames.add(new AnimationFrame(sprite.getTexCoordStartX(), sprite.getTexCoordStartY(),
                sprite.getTexCoordEndX(), sprite.getTexCoordEndY(), duration));
    }

    public void addFrames(String nameBase, int start, int end) {
        addFrames(nameBase, start, end, 1, frameDuration);
    }

    public void addFrames(String nameBase, int start, int end, int numDigits) {
        addFrames(nameBase, start, end, numDigits, frameDuration);
    }

    public void addFrames(String nameBase, int start, int end, int numDigits, float duration) {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(numDigits);

        for (int i = start; i <= end; i++) {
            String spriteName = nameBase + format.format(i);

            Sprite sprite = SpriteManager.getSprite(spriteName);
            addFrame(sprite, duration);
        }
    }

    @Override
    public boolean elapseTime(float seconds) {
        AnimationFrame curFrame = frames.get(currentFrameIndex);

        if (curFrame.duration != 0.0f) {
            curFrame.duration -= seconds;

            while (curFrame.duration < 0.0f) {
                if (currentFrameIndex == frames.size() - 1) {
                    numLoops--;
                    if (numLoops < 1) return true;

                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex++;
                }

                float oldDuration = curFrame.duration;
                curFrame.resetDuration();

                curFrame = frames.get(currentFrameIndex);

                // add any overrun from previous frame duration to the next one
                curFrame.duration += oldDuration;
            }
        }

        return super.elapseTime(seconds);
    }

    @Override
    public final void draw() {
        GL11.glColor4f(getR(), getG(), getB(), getA());
        GL14.glSecondaryColor3f(getR2(), getG2(), getB2());

        GL11.glPushMatrix();

        GL11.glTranslatef(getX(), getY(), 0.0f);

        if (getRotation() != 0.0f) GL11.glRotatef(getRotation(), 0.0f, 0.0f, 1.0f);

        AnimationFrame curFrame = frames.get(currentFrameIndex);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2d(curFrame.texCoordStartX, curFrame.texCoordStartY);
        GL11.glVertex2i(-halfWidth, -halfHeight);

        GL11.glTexCoord2d(curFrame.texCoordEndX, curFrame.texCoordStartY);
        GL11.glVertex2i(halfWidth, -halfHeight);

        GL11.glTexCoord2d(curFrame.texCoordEndX, curFrame.texCoordEndY);
        GL11.glVertex2i(halfWidth, halfHeight);

        GL11.glTexCoord2d(curFrame.texCoordStartX, curFrame.texCoordEndY);
        GL11.glVertex2i(-halfWidth, halfHeight);

        GL11.glEnd();

        GL11.glPopMatrix();
    }

    public void setColor(Color color) {
        if (color == null) return;

        setRed(color.getRedFloat());
        setGreen(color.getGreenFloat());
        setBlue(color.getBlueFloat());
    }

    @Override
    public Animated getCopy() {
        return new Animation(this);
    }

    private static class AnimationFrame {
        private final double texCoordStartX, texCoordStartY;
        private final double texCoordEndX, texCoordEndY;
        private final float initialDuration;
        private float duration;

        private AnimationFrame(double texCoordStartX, double texCoordStartY, double texCoordEndX,
                               double texCoordEndY, float duration) {
            this.texCoordStartX = texCoordStartX;
            this.texCoordStartY = texCoordStartY;
            this.texCoordEndX = texCoordEndX;
            this.texCoordEndY = texCoordEndY;

            this.duration = duration;
            initialDuration = duration;
        }

        private AnimationFrame(AnimationFrame other) {
            texCoordStartX = other.texCoordStartX;
            texCoordStartY = other.texCoordStartY;
            texCoordEndX = other.texCoordEndX;
            texCoordEndY = other.texCoordEndY;
            duration = other.duration;
            initialDuration = other.initialDuration;
        }

        private void resetDuration() {
            duration = initialDuration;
        }
    }
}

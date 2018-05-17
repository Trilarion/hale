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

import net.sf.hale.Game;
import net.sf.hale.entity.Location;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.resource.Sprite;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.util.*;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParticleGenerator implements Animated {
    private final String spriteRef;
    private Sprite particleSprite;
    private List<Particle> particles;
    private float speed;

    private Mode mode;
    private DrawingMode drawingMode;
    private boolean initialized;
    private boolean burstGenerated;
    private float timeLeft;
    private boolean stopAtOpaque, drawInOpaque;
    private float positionX, positionY;
    private float velocityX, velocityY;
    private float currentNewParticles;
    private float numParticles;
    private List<SubGenerator> subGenerators;
    private DistributionOneValue rotationDistribution;
    private DistributionOneValue rotationSpeedDistribution;
    private DistributionTwoValue velocityDistribution;
    private DistributionOneValue durationDistribution;
    private DistributionOneValue alphaVelocityDistribution;
    private DistributionOneValue redVelocityDistribution;
    private DistributionOneValue greenVelocityDistribution;
    private DistributionOneValue blueVelocityDistribution;
    private DistributionOneValue alphaDistribution;
    private DistributionOneValue redDistribution;
    private DistributionOneValue greenDistribution;
    private DistributionOneValue blueDistribution;
    private DistributionOneValue red2VelocityDistribution;
    private DistributionOneValue green2VelocityDistribution;
    private DistributionOneValue blue2VelocityDistribution;
    private DistributionOneValue red2Distribution;
    private DistributionOneValue green2Distribution;
    private DistributionOneValue blue2Distribution;
    public ParticleGenerator(ParticleGenerator other) {
        particles = new LinkedList<>();
        subGenerators = new LinkedList<>();

        initialized = other.initialized;

        if (other.velocityDistribution != null)
            velocityDistribution = other.velocityDistribution.getCopyIfHasState();
        if (other.durationDistribution != null)
            durationDistribution = other.durationDistribution.getCopyIfHasState();

        if (other.rotationDistribution != null)
            rotationDistribution = other.rotationDistribution.getCopyIfHasState();
        if (other.rotationSpeedDistribution != null)
            rotationSpeedDistribution = other.rotationSpeedDistribution.getCopyIfHasState();

        if (other.alphaVelocityDistribution != null)
            alphaVelocityDistribution = other.alphaVelocityDistribution.getCopyIfHasState();
        if (other.redVelocityDistribution != null)
            redVelocityDistribution = other.redVelocityDistribution.getCopyIfHasState();
        if (other.greenVelocityDistribution != null)
            greenVelocityDistribution = other.greenVelocityDistribution.getCopyIfHasState();
        if (other.blueVelocityDistribution != null)
            blueVelocityDistribution = other.blueVelocityDistribution.getCopyIfHasState();

        if (other.alphaDistribution != null)
            alphaDistribution = other.alphaDistribution.getCopyIfHasState();
        if (other.redDistribution != null)
            redDistribution = other.redDistribution.getCopyIfHasState();
        if (other.greenDistribution != null)
            greenDistribution = other.greenDistribution.getCopyIfHasState();
        if (other.blueDistribution != null)
            blueDistribution = other.blueDistribution.getCopyIfHasState();

        if (other.red2VelocityDistribution != null)
            red2VelocityDistribution = other.red2VelocityDistribution.getCopyIfHasState();
        if (other.green2VelocityDistribution != null)
            green2VelocityDistribution = other.green2VelocityDistribution.getCopyIfHasState();
        if (other.blue2VelocityDistribution != null)
            blue2VelocityDistribution = other.blue2VelocityDistribution.getCopyIfHasState();

        if (other.red2Distribution != null)
            red2Distribution = other.red2Distribution.getCopyIfHasState();
        if (other.green2Distribution != null)
            green2Distribution = other.green2Distribution.getCopyIfHasState();
        if (other.blue2Distribution != null)
            blue2Distribution = other.blue2Distribution.getCopyIfHasState();

        for (SubGenerator subGenerator : other.subGenerators) {
            subGenerators.add(new SubGenerator(subGenerator));
        }

        drawingMode = other.drawingMode;

        burstGenerated = other.burstGenerated;
        mode = other.mode;
        timeLeft = other.timeLeft;
        stopAtOpaque = other.stopAtOpaque;
        drawInOpaque = other.drawInOpaque;

        positionX = other.positionX;
        positionY = other.positionY;

        velocityX = other.velocityX;
        velocityY = other.velocityY;
        speed = other.speed;

        currentNewParticles = other.currentNewParticles;
        numParticles = other.numParticles;

        particleSprite = other.particleSprite;
        spriteRef = other.spriteRef;

        for (Particle particle : other.particles) {
            particles.add(new Particle(particle));
        }
    }

    public ParticleGenerator(Mode mode, String particleSprite, float numParticles) {
        this.mode = mode;
        drawingMode = DrawingMode.AboveEntities;

        this.particleSprite = SpriteManager.getSprite(particleSprite);

        spriteRef = particleSprite;

        this.numParticles = numParticles;

        particles = new LinkedList<>();

        subGenerators = new LinkedList<>();

        burstGenerated = false;
        initialized = false;
    }

    public static ParticleGenerator load(SimpleJSONObject data) throws LoadGameException {
        Mode mode = Mode.valueOf(data.get("generatorMode", null));
        String particleSprite = data.get("sprite", null);
        float numParticles = data.get("numParticles", 0.0f);

        ParticleGenerator generator = new ParticleGenerator(mode, particleSprite, numParticles);

        generator.loadBase(data);

        return generator;
    }

    @Override
    public JSONOrderedObject save() {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", getClass().getName());
        data.put("generatorMode", mode.toString());
        data.put("numParticles", numParticles);
        data.put("sprite", spriteRef);

        data.put("drawingMode", drawingMode.toString());
        data.put("burstGenerated", burstGenerated);
        data.put("timeLeft", timeLeft);
        data.put("stopAtOpaque", stopAtOpaque);
        data.put("drawInOpaque", drawInOpaque);
        data.put("positionX", positionX);
        data.put("positionY", positionY);
        data.put("velocityX", velocityX);
        data.put("velocityY", velocityY);
        data.put("currentNewParticles", currentNewParticles);

        JSONOrderedObject[] subGenData = new JSONOrderedObject[subGenerators.size()];
        int i = 0;
        for (SubGenerator subGenerator : subGenerators) {
            subGenData[i] = new JSONOrderedObject();
            subGenData[i].put("time", subGenerator.time);
            subGenData[i].put("animated", subGenerator.generator.save());
            i++;
        }

        if (subGenData.length != 0)
            data.put("subGenerators", subGenData);

        if (rotationDistribution != null)
            data.put("rotationDistribution", rotationDistribution.save());
        if (rotationSpeedDistribution != null)
            data.put("rotationSpeedDistribution", rotationSpeedDistribution.save());

        if (velocityDistribution != null)
            data.put("velocityDistribution", velocityDistribution.save());
        if (durationDistribution != null)
            data.put("durationDistribution", durationDistribution.save());

        if (alphaVelocityDistribution != null)
            data.put("alphaVelocityDistribution", alphaVelocityDistribution.save());
        if (redVelocityDistribution != null)
            data.put("redVelocityDistribution", redVelocityDistribution.save());
        if (greenVelocityDistribution != null)
            data.put("greenVelocityDistribution", greenVelocityDistribution.save());
        if (blueVelocityDistribution != null)
            data.put("blueVelocityDistribution", blueVelocityDistribution.save());

        if (alphaDistribution != null)
            data.put("alphaDistribution", alphaDistribution.save());
        if (redDistribution != null)
            data.put("redDistribution", redDistribution.save());
        if (greenDistribution != null)
            data.put("greenDistribution", greenDistribution.save());
        if (blueDistribution != null)
            data.put("blueDistribution", blueDistribution.save());

        if (red2VelocityDistribution != null)
            data.put("red2VelocityDistribution", red2VelocityDistribution.save());
        if (green2VelocityDistribution != null)
            data.put("green2VelocityDistribution", green2VelocityDistribution.save());
        if (blue2VelocityDistribution != null)
            data.put("blue2VelocityDistribution", blue2VelocityDistribution.save());

        if (red2Distribution != null)
            data.put("red2Distribution", red2Distribution.save());
        if (green2Distribution != null)
            data.put("green2Distribution", green2Distribution.save());
        if (blue2Distribution != null)
            data.put("blue2Distribution", blue2Distribution.save());

        return data;
    }

    /**
     * Loads the specified JSON data into this particle generator
     *
     * @param data the data to load
     */
    protected final void loadBase(SimpleJSONObject data) throws LoadGameException {
        drawingMode = DrawingMode.valueOf(data.get("drawingMode", null));
        burstGenerated = data.get("burstGenerated", false);
        timeLeft = data.get("timeLeft", 0.0f);
        stopAtOpaque = data.get("stopAtOpaque", false);
        drawInOpaque = data.get("drawInOpaque", false);
        positionX = data.get("positionX", 0.0f);
        positionY = data.get("positionY", 0.0f);
        velocityX = data.get("velocityX", 0.0f);
        velocityY = data.get("velocityY", 0.0f);
        currentNewParticles = data.get("currentNewParticles", 0.0f);

        if (data.containsKey("subGenerators")) {
            for (SimpleJSONArrayEntry entry : data.getArray("subGenerators")) {
                SimpleJSONObject entryData = entry.getObject();

                float time = entryData.get("anim", 0.0f);
                Animated anim = (Animated) SaveGameUtil.loadObject(entryData.getObject("animated"));

                addSubGenerator(anim, time);
            }
        }

        if (data.containsKey("rotationDistribution"))
            rotationDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("rotationDistribution"));
        if (data.containsKey("rotationSpeedDistribution"))
            rotationSpeedDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("rotationSpeedDistribution"));

        if (data.containsKey("velocityDistribution"))
            velocityDistribution = (DistributionTwoValue) SaveGameUtil.loadObject(data.getObject("velocityDistribution"));
        if (data.containsKey("durationDistribution"))
            durationDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("durationDistribution"));

        if (data.containsKey("alphaVelocityDistribution"))
            alphaVelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("alphaVelocityDistribution"));
        if (data.containsKey("redVelocityDistribution"))
            redVelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("redVelocityDistribution"));
        if (data.containsKey("greenVelocityDistribution"))
            greenVelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("greenVelocityDistribution"));
        if (data.containsKey("blueVelocityDistribution"))
            blueVelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("blueVelocityDistribution"));

        if (data.containsKey("alphaDistribution"))
            alphaDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("alphaDistribution"));
        if (data.containsKey("redDistribution"))
            redDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("redDistribution"));
        if (data.containsKey("greenDistribution"))
            greenDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("greenDistribution"));
        if (data.containsKey("blueDistribution"))
            blueDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("blueDistribution"));

        if (data.containsKey("red2VelocityDistribution"))
            red2VelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("red2VelocityDistribution"));
        if (data.containsKey("green2VelocityDistribution"))
            green2VelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("green2VelocityDistribution"));
        if (data.containsKey("blue2VelocityDistribution"))
            blue2VelocityDistribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("blue2VelocityDistribution"));

        if (data.containsKey("red2Distribution"))
            red2Distribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("red2Distribution"));
        if (data.containsKey("green2Distribution"))
            green2Distribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("green2Distribution"));
        if (data.containsKey("blue2Distribution"))
            blue2Distribution = (DistributionOneValue) SaveGameUtil.loadObject(data.getObject("blue2Distribution"));
    }

    @Override
    public DrawingMode getDrawingMode() {
        return drawingMode;
    }

    public void setDrawingMode(String drawingMode) {
        this.drawingMode = DrawingMode.valueOf(drawingMode);
    }

    @Override
    public boolean isDrawable() {
        return numParticles > 0.0f && particleSprite != null;
    }

    public void cacheSprite() {
    }

    public void setSprite(Sprite sprite) {
        particleSprite = sprite;
    }

    public final float getNumParticles() {
        return numParticles;
    }

    public void setNumParticles(float numParticles) {
        this.numParticles = numParticles;
    }

    public final float getX() {
        return positionX;
    }

    public final float getY() {
        return positionY;
    }

    public final float getSpeed() {
        return speed;
    }

    public void addSubGeneratorAtEnd(Animated generator) {
        subGenerators.add(new SubGenerator(generator, timeLeft));
    }

    public void addSubGenerator(Animated generator, float time) {
        subGenerators.add(new SubGenerator(generator, time));
    }

    public void setDrawParticlesInOpaque(boolean drawInOpaque) {
        this.drawInOpaque = drawInOpaque;
    }

    public void setStopParticlesAtOpaque(boolean stopAtOpaque) {
        this.stopAtOpaque = stopAtOpaque;
    }

    public void setPosition(Location location) {
        Point screenPoint = AreaUtil.convertGridToScreenAndCenter(location.getX(), location.getY());
        setPosition(screenPoint.x, screenPoint.y);
    }

    public void setPosition(Point gridPoint) {
        Point screenPoint = AreaUtil.convertGridToScreenAndCenter(gridPoint);
        setPosition(screenPoint.x, screenPoint.y);
    }

    public void offsetPosition(float x, float y) {
        positionX += x;
        positionY += y;
    }

    public void setPosition(float x, float y) {
        positionX = x;
        positionY = y;
    }

    public void setVelocity(float x, float y) {
        velocityX = x;
        velocityY = y;

        speed = (float) Math.sqrt(x * x + y * y);
    }

    public void setDurationInfinite() {
        timeLeft = 1.0e12f;
    }

    public void setDuration(float durationInSeconds) {
        timeLeft = durationInSeconds;
    }

    public float getTimeLeft() {
        return timeLeft;
    }

    private void setParametersBasedOnSpeed(Point startGrid, Point endGrid, float speed, boolean setRotation) {
        Point endScreen = AreaUtil.convertGridToScreenAndCenter(endGrid);
        Point startScreen = AreaUtil.convertGridToScreenAndCenter(startGrid);

        positionX = startScreen.x;
        positionY = startScreen.y;

        int distanceSquared = AreaUtil.euclideanDistance2(startScreen.x, startScreen.y, endScreen.x, endScreen.y);

        float distance = (float) Math.sqrt(distanceSquared);

        timeLeft = distance / speed;

        float distX = endScreen.x - startScreen.x;
        float distY = endScreen.y - startScreen.y;

        float angle = (float) Math.acos(distX / distance);

        if (distY < 0.0f) angle = -angle;

        velocityX = speed * (float) Math.cos(angle);
        velocityY = speed * (float) Math.sin(angle);
        this.speed = speed;

        if (setRotation) rotationDistribution = new FixedDistribution((float) (angle * 180.0 / Math.PI));
    }

    public void setVelocityDurationRotationBasedOnSpeed(Point startGrid, Point endGrid, float speed) {
        setParametersBasedOnSpeed(startGrid, endGrid, speed, true);
    }

    public void setVelocityDurationBasedOnSpeed(Point startGrid, Point endGrid, float speed) {
        setParametersBasedOnSpeed(startGrid, endGrid, speed, false);
    }

    public void setRotationDistribution(DistributionOneValue dist) {
        rotationDistribution = dist;
    }

    public void setRotationSpeedDistribution(DistributionOneValue dist) {
        rotationSpeedDistribution = dist;
    }

    public void setRedDistribution(DistributionOneValue dist) {
        redDistribution = dist;
    }

    public void setGreenDistribution(DistributionOneValue dist) {
        greenDistribution = dist;
    }

    public void setBlueDistribution(DistributionOneValue dist) {
        blueDistribution = dist;
    }

    public void setAlphaDistribution(DistributionOneValue dist) {
        alphaDistribution = dist;
    }

    public void setRedSpeedDistribution(DistributionOneValue dist) {
        redVelocityDistribution = dist;
    }

    public void setGreenSpeedDistribution(DistributionOneValue dist) {
        greenVelocityDistribution = dist;
    }

    public void setBlueSpeedDistribution(DistributionOneValue dist) {
        blueVelocityDistribution = dist;
    }

    public void setAlphaSpeedDistribution(DistributionOneValue dist) {
        alphaVelocityDistribution = dist;
    }

    public void setSecondaryRedDistribution(DistributionOneValue dist) {
        red2Distribution = dist;
    }

    public void setSecondaryGreenDistribution(DistributionOneValue dist) {
        green2Distribution = dist;
    }

    public void setSecondaryBlueDistribution(DistributionOneValue dist) {
        blue2Distribution = dist;
    }

    public void setSecondaryRedSpeedDistribution(DistributionOneValue dist) {
        red2VelocityDistribution = dist;
    }

    public void setSecondaryGreenSpeedDistribution(DistributionOneValue dist) {
        green2VelocityDistribution = dist;
    }

    public void setSecondaryBlueSpeedDistribution(DistributionOneValue dist) {
        blue2VelocityDistribution = dist;
    }

    public void setDurationDistribution(DistributionOneValue distribution) {
        durationDistribution = distribution;
    }

    public void setVelocityDistribution(DistributionTwoValue distribution) {
        velocityDistribution = distribution;
    }

    public boolean elapseTime(float seconds) {
        timeLeft -= seconds;

        Iterator<SubGenerator> subIter = subGenerators.iterator();
        while (subIter.hasNext()) {
            SubGenerator subGenerator = subIter.next();

            subGenerator.time -= seconds;

            if (subGenerator.time < 0.0f) {
                subGenerator.generator.setPosition(positionX, positionY);
                Game.particleManager.add(subGenerator.generator);
                subIter.remove();
            }
        }

        offsetPosition(velocityX * seconds, velocityY * seconds);

        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            boolean finished = iter.next().elapseTime(seconds);

            if (finished) {
                iter.remove();
            }
        }

        if (timeLeft > 0.0f) {
            switch (mode) {
                case Continuous:
                    currentNewParticles += seconds * numParticles;
                    int particlesToAdd = (int) Math.floor(currentNewParticles);
                    addParticles(particlesToAdd);
                    currentNewParticles -= particlesToAdd;
                    break;
                case Burst:
                    if (!burstGenerated) {
                        addParticles((int) numParticles);
                        burstGenerated = true;
                    }
                    break;
            }
        }

        return (timeLeft < 0.0f && particles.isEmpty());
    }

    // sub classes can override to do any needed startup
    public boolean initialize() {
        boolean alreadyInitialized = initialized;

        initialized = true;

        return !alreadyInitialized;
    }

    // sub classes can override to give different kinds of particle sources (lines, rectangles, etc)
    protected void setParticlePosition(Particle particle) {
        particle.setPosition(positionX, positionY);
    }

    private void addParticles(int num) {
        for (int i = 0; i < num; i++) {
            Particle p = new Particle(particleSprite);
            p.setStopAtOpaque(stopAtOpaque);
            p.setDrawInOpaque(drawInOpaque);

            setParticlePosition(p);

            if (durationDistribution != null) p.setDuration(durationDistribution.generate(p));
            else p.setDuration(timeLeft);

            if (velocityDistribution != null) p.setVelocity(velocityDistribution.generate(p));

            if (alphaDistribution != null) p.setAlpha(alphaDistribution.generate(p));
            if (redDistribution != null) p.setRed(redDistribution.generate(p));
            if (greenDistribution != null) p.setGreen(greenDistribution.generate(p));
            if (blueDistribution != null) p.setBlue(blueDistribution.generate(p));

            if (red2Distribution != null) p.setSecondaryRed(red2Distribution.generate(p));
            if (green2Distribution != null) p.setSecondaryGreen(green2Distribution.generate(p));
            if (blue2Distribution != null) p.setSecondaryBlue(blue2Distribution.generate(p));

            if (alphaVelocityDistribution != null) p.setAlphaSpeed(alphaVelocityDistribution.generate(p));
            if (redVelocityDistribution != null) p.setRedSpeed(redVelocityDistribution.generate(p));
            if (greenVelocityDistribution != null) p.setGreenSpeed(greenVelocityDistribution.generate(p));
            if (blueVelocityDistribution != null) p.setBlueSpeed(blueVelocityDistribution.generate(p));

            if (red2VelocityDistribution != null) p.setSecondaryRedSpeed(red2VelocityDistribution.generate(p));
            if (green2VelocityDistribution != null) p.setSecondaryGreenSpeed(green2VelocityDistribution.generate(p));
            if (blue2VelocityDistribution != null) p.setSecondaryBlueSpeed(blue2VelocityDistribution.generate(p));

            if (rotationDistribution != null) p.setRotation(rotationDistribution.generate(p));
            if (rotationSpeedDistribution != null) p.setRotationSpeed(rotationSpeedDistribution.generate(p));

            particles.add(p);
        }
    }

    public void draw() {
        // it is a big performance improvement to bind the texture only once
        // this assumes that all particles in this generator use a texture from the same spritesheet

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, particleSprite.getTextureReference());

        for (Particle particle : particles) {
            particle.draw();
        }
    }

    @Override
    public Animated getCopy() {
        return new ParticleGenerator(this);
    }

    public enum Mode {
        Continuous, Burst,
    }

    private static class SubGenerator {
        private Animated generator;
        private float time;

        private SubGenerator(SubGenerator other) {
            generator = other.generator.getCopy();
            time = other.time;
        }
        private SubGenerator(Animated generator, float time) {
            this.generator = generator;
            this.time = time;
        }
    }
}

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

package net.sf.hale.ability;

import net.sf.hale.Game;
import net.sf.hale.entity.Entity;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.ReferenceHandler;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;
import net.sf.hale.util.SimpleJSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An Aura is a special kind of Effect that travels with its target
 *
 * @author Jared Stephen
 */
public class Aura extends Effect {
    private int maxRadius;
    private int minRadius;

    /**
     * Create a new Aura using the script with the specified script ID.
     *
     * @param scriptID
     */
    public Aura(String scriptID) {
        super(scriptID);

        minRadius = 0;
        maxRadius = 0;
    }

    /**
     * Creates an exact copy of the specified Aura
     *
     * @param other  the Aura being copied
     * @param target
     */
    public Aura(Aura other, EffectTarget target) {
        super(other, target);

        maxRadius = other.maxRadius;
        minRadius = other.minRadius;
    }

    public static Aura load(SimpleJSONObject data, ReferenceHandler refHandler) {
        Aura aura = new Aura(data.get("scriptLocation", null));
        aura.minRadius = data.get("minRadius", 0);
        aura.maxRadius = data.get("maxRadius", 0);

        return aura;
    }

    @Override
    public JSONOrderedObject save() {
        JSONOrderedObject data = super.save();

        data.put("isAura", true);
        data.put("minRadius", minRadius);
        data.put("maxRadius", maxRadius);

        return data;
    }

    /**
     * Returns the minimum radius around the target creature that this Aura affects.  Creatures
     * inside this radius are unaffected
     *
     * @return the radius for this aura
     */
    public int getAuraMinRadius() {
        return minRadius;
    }

    /**
     * Sets the minimum radius around the target creature that this Aura affects. Creatures
     * inside this radius are unaffected
     *
     * @param radius the radius for this aura
     */
    public void setAuraMinRadius(int radius) {
        minRadius = radius;
    }

    /**
     * Returns the radius around the target creature that this Aura affects
     *
     * @return the radius for this aura
     */
    public int getAuraMaxRadius() {
        return maxRadius;
    }

    /**
     * Sets the radius around the target creature that this Aura affects
     *
     * @param radius the radius for this aura
     */
    public void setAuraMaxRadius(int radius) {
        maxRadius = radius;
    }

    /**
     * Computes the list of points in the area currently affected by this Aura, based on this
     * position of the target.  If there is no target or the target is not
     * an entity, then the returned list will be empty
     *
     * @return the list of points affected by this Aura
     */
    public List<Point> getCurrentAffectedPoints() {
        if (!(getTarget() instanceof Entity))
            return Collections.emptyList();

        int height = Game.curCampaign.curArea.getHeight();
        int width = Game.curCampaign.curArea.getWidth();
        Point center = ((Entity) getTarget()).getLocation().toPoint();

        List<Point> points = new ArrayList<>();

        if (minRadius <= 0) points.add(center);

        for (int r = Math.max(1, minRadius); r <= maxRadius; r++) {
            for (int i = 0; i < r * 6; i++) {
                Point current = AreaUtil.convertPolarToGrid(center, r, i);

                if (current.x < 0 || current.x >= width) continue;
                if (current.y < 0 || current.y >= height) continue;

                points.add(current);
            }
        }

        return points;
    }
}

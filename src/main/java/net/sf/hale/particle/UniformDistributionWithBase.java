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
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.util.SaveGameUtil;
import net.sf.hale.util.SimpleJSONObject;

public class UniformDistributionWithBase implements DistributionOneValue {
    private final DistributionBase base;
    private final float plusOrMinusFraction;
    private final float multiplier;
    private final float offset;

    public UniformDistributionWithBase(DistributionBase base, float multiplier, float offset, float plusOrMinusFraction) {
        this.base = base;
        this.plusOrMinusFraction = plusOrMinusFraction;
        this.multiplier = multiplier;
        this.offset = offset;
    }

    public static UniformDistributionWithBase load(SimpleJSONObject data) throws LoadGameException {
        float multiplier = data.get("multiplier", 0.0f);
        float offset = data.get("offset", 0.0f);
        float plusOrMinusFraction = data.get("plusOrMinusFraction", 0.0f);
        DistributionBase base = (DistributionBase) SaveGameUtil.loadObject(data.getObject("base"));

        return new UniformDistributionWithBase(base, multiplier, offset, plusOrMinusFraction);
    }

    @Override
    public Object save() {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", getClass().getName());
        data.put("plusOrMinusFraction", plusOrMinusFraction);
        data.put("multiplier", multiplier);
        data.put("offset", offset);
        data.put("base", base.save());

        return data;
    }

    @Override
    public float generate(Particle particle) {
        float avg = base.getBase(particle) * multiplier + offset;

        return Game.dice.rand(avg - avg * plusOrMinusFraction, avg + avg * plusOrMinusFraction);
    }

    @Override
    public DistributionOneValue getCopyIfHasState() {
        return this;
    }
}

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

import net.sf.hale.loading.Saveable;

/**
 * A one-valued Distribution.  A distribution allows players to specify the possible
 * values that a given attribute of a {@link Particle} will possess.  The Distribution is
 * supplied to the {@link ParticleGenerator}, which then generates particles with attributes
 * based on the distributions supplied.  For example, you can specify a random distribution
 * for the velocity, position, or color of the particles generated by the Particle Generator.
 * <p>
 * This distribution supplies only a single value, so it is useful for scalar based quantities.
 *
 * @author Jared Stephen
 */
public interface DistributionOneValue extends Saveable {

    /**
     * Generate a value from this distribution for the given particle.  The value
     * may or may not depend on the state of the supplied particle.
     *
     * @param particle the Particle to generate a value for.
     * @return a generated float value for use with the supplied particle
     */
    float generate(Particle particle);

    /**
     * If this DistributionOneValue has any internal state, returns a copy
     * of this.  If this DistributionOneValue does not have any state, returns
     * this DistributionOneValue.
     *
     * @return a copy of this Distribution or this Distribution
     */
    DistributionOneValue getCopyIfHasState();
}

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

package net.sf.hale.bonus;

import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.util.SimpleJSONObject;

public class DamageBonus extends IntBonus implements BonusWithSuperType {
    private final String damageType;

    public DamageBonus(Type type, StackType stackType, String damageType, int value) {
        super(type, stackType, value);

        this.damageType = damageType;
    }

    public static DamageBonus load(SimpleJSONObject data) {
        int value = data.get("value", 0);
        Type type = Type.valueOf(data.get("type", null));
        StackType stackType = StackType.valueOf(data.get("stackType", null));
        String damageType = data.get("damageType", null);

        return new DamageBonus(type, stackType, damageType, value);
    }

    @Override
    public JSONOrderedObject save() {
        JSONOrderedObject data = super.save();

        data.put("damageType", damageType);

        return data;
    }

    public String getDamageType() {
        return damageType;
    }

    @Override
    public String getSuperType() {
        return damageType;
    }

    @Override
    public DamageBonus cloneWithReduction(int reduction) {
        return new DamageBonus(getType(), getStackType(), damageType, getValue() - reduction);
    }

    @Override
    public void appendDescription(StringBuilder sb) {
        super.appendDescription(sb);
        sb.append(" for ");
        sb.append(damageType);
    }
}

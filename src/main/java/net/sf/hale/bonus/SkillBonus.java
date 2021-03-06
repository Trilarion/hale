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

import net.sf.hale.Game;
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.util.SimpleJSONObject;

public class SkillBonus extends IntBonus implements BonusWithSuperType {
    private final String skillID;

    public SkillBonus(String skillID, StackType stackType, int value) {
        super(Type.Skill, stackType, value);
        this.skillID = skillID;
    }

    public static SkillBonus load(SimpleJSONObject data) {
        int value = data.get("value", 0);
        StackType stackType = StackType.valueOf(data.get("stackType", null));
        String skillID = data.get("skillID", null);

        return new SkillBonus(skillID, stackType, value);
    }

    @Override
    public JSONOrderedObject save() {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", getClass().getName());
        data.put("stackType", getStackType().toString());
        data.put("value", getValue());
        data.put("skillID", skillID);

        return data;
    }

    public String getSkillID() {
        return skillID;
    }

    @Override
    public String getSuperType() {
        return skillID;
    }

    @Override
    public SkillBonus cloneWithReduction(int reduction) {
        return new SkillBonus(skillID, getStackType(), getValue() - reduction);
    }

    @Override
    public void appendDescription(StringBuilder sb) {
        sb.append("<span style=\"font-family: blue;\">");
        sb.append(Game.ruleset.getSkill(skillID).getNoun());
        sb.append("</span> Skill ");

        super.appendDescription(sb);
    }
}

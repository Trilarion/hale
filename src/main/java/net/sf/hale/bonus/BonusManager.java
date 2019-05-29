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
import net.sf.hale.bonus.Bonus.StackType;
import net.sf.hale.bonus.Bonus.Type;
import net.sf.hale.entity.Creature;
import net.sf.hale.rules.Damage;
import net.sf.hale.rules.DamageType;

import java.util.HashMap;
import java.util.Map;

public class BonusManager {
    private final ProficiencyList weaponProficiencies;
    private final ProficiencyList armorProficiencies;
    private final StandaloneDamageBonusList standaloneDamageBonuses;

    private final Map<Type, BonusTypeList> bonuses;
    private final Map<String, BonusSuperTypeList> bonusesWithSuperType;

    public BonusManager() {
        bonuses = new HashMap<>();

        weaponProficiencies = new ProficiencyList();
        armorProficiencies = new ProficiencyList();

        bonusesWithSuperType = new HashMap<>();

        standaloneDamageBonuses = new StandaloneDamageBonusList();
    }

    public BonusManager(BonusManager other) {
        bonuses = new HashMap<>();
        for (Type key : other.bonuses.keySet()) {
            BonusTypeList list = new BonusTypeList(other.bonuses.get(key));
            bonuses.put(key, list);
        }

        weaponProficiencies = new ProficiencyList(other.weaponProficiencies);
        armorProficiencies = new ProficiencyList(other.armorProficiencies);

        bonusesWithSuperType = new HashMap<>();
        for (String key : other.bonusesWithSuperType.keySet()) {
            BonusSuperTypeList list = new BonusSuperTypeList(other.bonusesWithSuperType.get(key));
            bonusesWithSuperType.put(key, list);
        }

        standaloneDamageBonuses = new StandaloneDamageBonusList(other.standaloneDamageBonuses);
    }

    public void removeAll(BonusList bonuses) {
        for (Bonus bonus : bonuses) {
            remove(bonus);
        }
    }

    public void remove(Bonus bonus) {
        if (bonuses.containsKey(bonus.getType())) {
            bonuses.get(bonus.getType()).remove(bonus);

            if (bonuses.get(bonus.getType()).isEmpty()) {
                bonuses.remove(bonus.getType());
            }
        }

        switch (bonus.getType()) {
            case ArmorProficiency:
                armorProficiencies.remove(((ArmorProficiency) bonus).getArmorType());
                break;
            case WeaponProficiency:
                weaponProficiencies.remove(((WeaponProficiency) bonus).getBaseWeapon());
                break;
            case Skill:
            case AttackVsRacialType:
            case DamageVsRacialType:
            case ArmorClassVsRacialType:
            case ArmorTypeMovementPenalty:
            case ArmorTypeArmorPenalty:
            case ArmorTypeArmorClass:
            case BaseWeaponAttack:
            case BaseWeaponDamage:
            case BaseWeaponSpeed:
            case BaseWeaponCriticalChance:
            case BaseWeaponCriticalMultiplier:
            case DamageImmunity:
            case DamageReduction:
            case DamageForWeaponType:
            case DamageForSpellType:
            case AttackForWeaponType:
                String superType = ((BonusWithSuperType) bonus).getSuperType();

                if (bonusesWithSuperType.get(superType) != null)
                    bonusesWithSuperType.get(superType).remove(bonus);
                break;
            case StandaloneDamage:
                standaloneDamageBonuses.remove(bonus);
            default:
                break;
        }
    }

    public void addAll(BonusList bonuses) {
        if (bonuses == null) return;

        for (Bonus bonus : bonuses) {
            add(bonus);
        }
    }

    public void add(Bonus bonus) {
        // bonuses with sub types need to be summed separately as special cases
        switch (bonus.getType()) {
            case ArmorProficiency:
                String armorType = ((ArmorProficiency) bonus).getArmorType();
                armorProficiencies.add(armorType);
                break;
            case WeaponProficiency:
                String baseWeapon = ((WeaponProficiency) bonus).getBaseWeapon();
                weaponProficiencies.add(baseWeapon);
                break;
            case Skill:
            case AttackVsRacialType:
            case DamageVsRacialType:
            case ArmorClassVsRacialType:
            case ArmorTypeMovementPenalty:
            case ArmorTypeArmorPenalty:
            case ArmorTypeArmorClass:
            case BaseWeaponAttack:
            case BaseWeaponDamage:
            case BaseWeaponSpeed:
            case BaseWeaponCriticalChance:
            case BaseWeaponCriticalMultiplier:
            case DamageImmunity:
            case DamageReduction:
            case DamageForWeaponType:
            case DamageForSpellType:
            case AttackForWeaponType:
                String superType = ((BonusWithSuperType) bonus).getSuperType();
                if (bonusesWithSuperType.containsKey(superType)) {
                    bonusesWithSuperType.get(superType).add(bonus);
                } else {
                    BonusSuperTypeList bonusSuperTypeList = new BonusSuperTypeList();
                    bonusSuperTypeList.add(bonus);
                    bonusesWithSuperType.put(superType, bonusSuperTypeList);
                }
                break;
            case StandaloneDamage:
                standaloneDamageBonuses.add(bonus);
            default:
                if (bonuses.containsKey(bonus.getType())) {
                    bonuses.get(bonus.getType()).add(bonus);
                } else {
                    BonusTypeList bonusTypeList = new BonusTypeList();
                    bonusTypeList.add(bonus);
                    bonuses.put(bonus.getType(), bonusTypeList);
                }
        }
    }

    public boolean has(Type type) {
        return bonuses.containsKey(type);
    }

    public int get(String superType, Type type) {
        if (bonusesWithSuperType.containsKey(superType))
            return bonusesWithSuperType.get(superType).getCurrentTotal(type);
        else return 0;
    }

    public int get(Type type) {
        if (bonuses.containsKey(type)) return bonuses.get(type).getCurrentTotal();
        else return 0;
    }

    public int get(Type type, StackType stackType) {
        if (bonuses.containsKey(type)) return bonuses.get(type).get(stackType);
        else return 0;
    }

    public boolean hasWeaponProficiency(String baseWeapon) {
        return weaponProficiencies.hasProficiency(baseWeapon);
    }

    public boolean hasArmorProficiency(String armorType) {
        return armorProficiencies.hasProficiency(armorType);
    }

    public int getSkillBonus(String skillID) {
        if (bonusesWithSuperType.containsKey(skillID))
            return bonusesWithSuperType.get(skillID).getCurrentTotal(Type.Skill);
        else return 0;
    }

    public Damage rollStandaloneDamage(Creature parent) {
        return standaloneDamageBonuses.roll(parent);
    }

    public void clear() {
        weaponProficiencies.clear();
        armorProficiencies.clear();
        bonuses.clear();
        bonusesWithSuperType.clear();
        standaloneDamageBonuses.clear();
    }

    public int getDamageReduction(DamageType damageType) {
        int dr = get(damageType.getName(), Type.DamageReduction);

        if (damageType.isPhysical()) {
            return dr + get(Game.ruleset.getString("PhysicalDamageType"), Type.DamageReduction);
        } else {
            return dr;
        }
    }

    public int getDamageImmunity(DamageType damageType) {
        int di = get(damageType.getName(), Type.DamageImmunity);

        if (damageType.isPhysical()) {
            return di + get(Game.ruleset.getString("PhysicalDamageType"), Type.DamageImmunity);
        } else {
            return di;
        }
    }

    public int getAppliedDamage(int damage, DamageType damageType) {
        if (damageType == null) return damage;

        int reduction = 0;
        int immunity = 0;

        BonusSuperTypeList list = bonusesWithSuperType.get(damageType.getName());
        if (list != null) {
            reduction += list.getCurrentTotal(Type.DamageReduction);
            immunity += list.getCurrentTotal(Type.DamageImmunity);
        }

        if (damageType.isPhysical()) {
            BonusSuperTypeList physicalList = bonusesWithSuperType.get(Game.ruleset.getString("PhysicalDamageType"));
            if (physicalList != null) {
                reduction += physicalList.getCurrentTotal(Type.DamageReduction);
                immunity += physicalList.getCurrentTotal(Type.DamageImmunity);
            }
        }

        if (immunity > 100) immunity = 100;
        if (reduction > damage) reduction = damage;

        int percentageAmount = (damage - reduction) * immunity / 100;

        return damage - reduction - percentageAmount;
    }
}

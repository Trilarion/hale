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
import net.sf.hale.ability.Effect;
import net.sf.hale.bonus.Bonus.StackType;
import net.sf.hale.bonus.Bonus.Type;
import net.sf.hale.entity.*;
import net.sf.hale.entity.Inventory.Slot;
import net.sf.hale.rules.Damage;
import net.sf.hale.rules.DamageType;
import net.sf.hale.rules.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatManager {
    private final Creature parent;
    private final Map<Stat, Integer> stats;
    private final BonusManager bonuses;

    public StatManager(Creature parent) {
        this.parent = parent;
        bonuses = new BonusManager();
        stats = new HashMap<>();
    }

    public StatManager(StatManager other, Creature parent) {
        this.parent = parent;

        bonuses = new BonusManager(other.bonuses);

        stats = new HashMap<>(other.stats);
    }

    public void removeEffectPenaltiesOfType(String bonusType) {
        Type type = Bonus.parseType(bonusType);

        removeAll(parent.getEffects().getPenaltiesOfType(type));
    }

    public void removeEffectBonusesOfType(String bonusType) {
        Type type = Bonus.parseType(bonusType);

        removeAll(parent.getEffects().getBonusesOfType(type));
    }

    public int reducePenaltiesOfTypeByAmount(String bonusType, int amount) {
        if (amount == 0) return 0;

        BonusList bonusesToRemove = new BonusList();
        BonusList bonusesToAdd = new BonusList();

        Type type = Bonus.parseType(bonusType);

        int amountLeft = amount;

        List<Bonus> bonusesToRemoveFromEffect = new ArrayList<>();

        for (Effect effect : parent.getEffects().getEffectsWithBonusesOfType(type)) {
            bonusesToRemoveFromEffect.clear();

            for (Bonus bonus : effect.getBonuses()) {
                if (bonus.getType() == type) {
                    int bonusValue = -bonus.getValue();

                    if (bonusValue >= amountLeft) {
                        Bonus newBonus = bonus.cloneWithReduction(-amountLeft);
                        amountLeft = 0;

                        effect.getBonuses().add(newBonus);
                        bonusesToRemoveFromEffect.add(bonus);

                        bonusesToRemove.add(bonus);
                        bonusesToAdd.add(newBonus);
                    } else {
                        bonusesToRemove.add(bonus);
                        bonusesToRemoveFromEffect.add(bonus);

                        amountLeft -= bonusValue;
                    }
                }

                if (amountLeft == 0) break;
            }

            for (Bonus bonus : bonusesToRemoveFromEffect)
                effect.getBonuses().remove(bonus);

            if (amountLeft == 0) break;
        }

        removeAll(bonusesToRemove);
        addAll(bonusesToAdd);

        return amountLeft;
    }

    public void changeEquipment(EquippableItemTemplate.Type itemType) {
        switch (itemType) {
            case Weapon:
            case Shield:
            case Ammo:
                // recompute attack bonus for shield swaps due to shield attack penalty
                recomputeAttackBonus();
                // recompute defense for weapon swaps for a few fringe cases where weapons affect your AC
            case Armor:
            case Gloves:
            case Helmet:
            case Boots:
                recomputeArmorClass();
                break;
            default:
        }
    }

    private void checkRecompute(BonusList bonuses, RecomputeMode mode, int oldConBonus) {
        boolean recomputeArmorClass = false;
        boolean recomputeAttackBonus = false;
        boolean recomputeStr = false;
        boolean recomputeDex = false;
        boolean recomputeCon = false;
        boolean recomputeInt = false;
        boolean recomputeWis = false;
        boolean recomputeCha = false;

        for (Bonus bonus : bonuses) {
            switch (bonus.getType()) {
                case Immobilized:
                case Initiative:
                case ArmorClass:
                case ArmorPenalty:
                case ArmorTypeArmorPenalty:
                case ArmorTypeMovementPenalty:
                case ArmorTypeArmorClass:
                case DualWieldArmorClass:
                case Movement:
                case ImmobilizationImmunity:
                case UndispellableImmobilized:
                    recomputeArmorClass = true;
                    break;
                case Attack:
                case Damage:
                case AttackCost:
                case ShieldAttack:
                case CriticalChance:
                case CriticalMultiplier:
                case MainHandAttack:
                case OffHandAttack:
                case MainHandDamage:
                case OffHandDamage:
                case BaseWeaponAttack:
                case BaseWeaponDamage:
                case BaseWeaponSpeed:
                case BaseWeaponCriticalChance:
                case BaseWeaponCriticalMultiplier:
                case LightMeleeWeaponDamage:
                case OneHandedMeleeWeaponDamage:
                case TwoHandedMeleeWeaponDamage:
                case RangedDamage:
                case RangedAttack:
                case DualWieldAttack:
                case DualWieldStrDamage:
                case LightMeleeWeaponAttack:
                case OneHandedMeleeWeaponAttack:
                case TwoHandedMeleeWeaponAttack:
                    recomputeAttackBonus = true;
                    break;
                case BaseStr:
                case Str:
                    recomputeStr = true;
                    break;
                case BaseDex:
                case Dex:
                    recomputeDex = true;
                    break;
                case BaseCon:
                case Con:
                    recomputeCon = true;
                    break;
                case BaseInt:
                case Int:
                    recomputeInt = true;
                    break;
                case BaseWis:
                case Wis:
                    recomputeWis = true;
                    break;
                case BaseCha:
                case Cha:
                    recomputeCha = true;
                    break;
                case TemporaryHP:
                    switch (mode) {
                        case Removal:
                            parent.removeTemporaryHitPoints(bonus.getValue());
                            break;
                        case Addition:
                            parent.addTemporaryHitPoints(bonus.getValue());
                            break;
                    }
                    break;
                default:
            }
        }

        if (recomputeStr && recomputeDex) {
            recomputeStrNoAttackBonus();
            recomputeDex();
            recomputeArmorClass = false;
            recomputeAttackBonus = false;
        } else if (recomputeStr) {
            recomputeStr();
            recomputeAttackBonus = false;
        } else if (recomputeDex) {
            recomputeDex();
            recomputeArmorClass = false;
            recomputeAttackBonus = false;
        }

        if (recomputeCon) recomputeCon();
        if (recomputeInt) recomputeInt();
        if (recomputeWis) recomputeWis();
        if (recomputeCha) recomputeCha();

        if (recomputeArmorClass) recomputeArmorClass();
        if (recomputeAttackBonus) recomputeAttackBonus();

        int currentConBonus = get(Type.Con);

        if (currentConBonus != oldConBonus) {
            int oldConHP = (getCon() - currentConBonus + oldConBonus - 10) * get(Stat.CreatureLevel) / 3;
            int newConHP = (getCon() - 10) * get(Stat.CreatureLevel) / 3;

            if (oldConHP > newConHP) {
                parent.takeDamage(oldConHP - newConHP, "Effect");
            } else if (oldConHP < newConHP) {
                parent.healDamage(newConHP - oldConHP);
            }
        }
    }

    public void removeAll(BonusList bonuses) {
        int oldConBonus = get(Type.Con);

        this.bonuses.removeAll(bonuses);

        checkRecompute(bonuses, RecomputeMode.Removal, oldConBonus);
    }

    public void addAll(BonusList bonuses) {
        int oldConBonus = get(Type.Con);

        this.bonuses.addAll(bonuses);

        checkRecompute(bonuses, RecomputeMode.Addition, oldConBonus);
    }

    public void addAllNoRecompute(BonusList bonuses) {
        this.bonuses.addAll(bonuses);
    }

    public boolean has(String type) {
        return has(Bonus.parseType(type));
    }

    public boolean has(Type type) {
        return bonuses.has(type);
    }

    public int get(String superType, Type type) {
        return bonuses.get(superType, type);
    }

    public int getDamageReduction(String damageType) {
        return bonuses.getDamageReduction(Game.ruleset.getDamageType(damageType));
    }

    public int getDamageReduction(DamageType damageType) {
        return bonuses.getDamageReduction(damageType);
    }

    public int getDamageImmunity(String damageType) {
        return bonuses.getDamageImmunity(Game.ruleset.getDamageType(damageType));
    }

    public int getDamageImmunity(DamageType damageType) {
        return bonuses.getDamageImmunity(damageType);
    }

    public int get(Type type, StackType stackType) {
        return bonuses.get(type, stackType);
    }

    public int get(Type type, StackType... stackTypes) {
        int total = 0;

        for (StackType t : stackTypes) {
            total += bonuses.get(type, t);
        }

        return total;
    }

    public int get(Type type) {
        return bonuses.get(type);
    }

    public int get(Stat stat) {
        if (stats.containsKey(stat)) return stats.get(stat);
        else return 0;
    }

    public boolean hasWeaponProficiency(String baseWeapon) {
        if (baseWeapon.equals(Game.ruleset.getString("DefaultBaseWeapon"))) return true;

        return bonuses.hasWeaponProficiency(baseWeapon);
    }

    public boolean hasArmorProficiency(String armorType) {
        if (armorType.equals(Game.ruleset.getString("DefaultArmorType"))) return true;

        return bonuses.hasArmorProficiency(armorType);
    }

    public int getSkillBonus(String skillID) {
        return bonuses.getSkillBonus(skillID);
    }

    public Damage rollStandaloneDamage(Creature parent) {
        return bonuses.rollStandaloneDamage(parent);
    }

    public int getAppliedDamage(int damage, DamageType damageType) {
        return bonuses.getAppliedDamage(damage, damageType);
    }

    private void addToStat(Stat stat, int addedValue) {
        int currentValue = stats.get(stat);

        stats.put(stat, currentValue + addedValue);
    }

    private void zeroStats(Stat... statsToZero) {
        for (Stat stat : statsToZero) {
            stats.put(stat, 0);
        }
    }

    public void recomputeStr() {
        stats.put(Stat.Str, getBaseStr() + get(Type.Str));
        recomputeWeightLimit();
        recomputeAttackBonus();
    }

    // for use when recomputing all stats so we don't compute attackBonus twice - once for str and again for dex
    private void recomputeStrNoAttackBonus() {
        stats.put(Stat.Str, getBaseStr() + get(Type.Str));
        recomputeWeightLimit();
    }

    public void recomputeDex() {
        stats.put(Stat.Dex, getBaseDex() + get(Type.Dex));
        recomputeReflexResistance();
        recomputeArmorClass();
        recomputeAttackBonus();
    }

    public void recomputeCon() {
        stats.put(Stat.Con, getBaseCon() + get(Type.Con));
        recomputeLevelAndMaxHP();
        recomputePhysicalResistance();
    }

    public void recomputeInt() {
        stats.put(Stat.Int, getBaseInt() + get(Type.Int));
    }

    public void recomputeWis() {
        stats.put(Stat.Wis, getBaseWis() + get(Type.Wis));
        recomputeMentalResistance();
    }

    public void recomputeCha() {
        stats.put(Stat.Cha, getBaseCha() + get(Type.Cha));
    }

    public void recomputeLevelAndMaxHP() {
        zeroStats(Stat.LevelAttackBonus, Stat.LevelDamageBonus, Stat.MaxHP);

        stats.put(Stat.CasterLevel, parent.roles.getCasterLevel());
        stats.put(Stat.CreatureLevel, parent.roles.getTotalLevel());

        for (String roleID : parent.roles.getRoleIDs()) {
            Role role = Game.ruleset.getRole(roleID);
            int level = parent.roles.getLevel(roleID);

            addToStat(Stat.LevelAttackBonus, level * role.getAttackBonusPerLevel());
            addToStat(Stat.LevelDamageBonus, level * role.getDamageBonusPerLevel());

            if (role == parent.roles.getBaseRole()) {
                addToStat(Stat.MaxHP, role.getHPAtLevelOne() + (level - 1) * role.getHPPerLevel());
            } else {
                addToStat(Stat.MaxHP, level * role.getHPPerLevel());
            }
        }

        addToStat(Stat.MaxHP, (getCon() - 10) * get(Stat.CreatureLevel) / 3);

        recomputeMentalResistance();
        recomputePhysicalResistance();
        recomputeReflexResistance();
    }

    public void recomputeMentalResistance() {
        stats.put(Stat.MentalResistance, (getWis() - 10) * 2 + getCreatureLevel() * 3);
    }

    public void recomputePhysicalResistance() {
        stats.put(Stat.PhysicalResistance, (getCon() - 10) * 2 + getCreatureLevel() * 3);
    }

    public void recomputeReflexResistance() {
        stats.put(Stat.ReflexResistance, (getDex() - 10) * 2 + getCreatureLevel() * 3);
    }

    public void recomputeWeightLimit() {
        stats.put(Stat.WeightLimit, Game.ruleset.getValue("WeightLimitBase") +
                (getStr() - 10) * Game.ruleset.getValue("WeightLimitStrengthFactor"));
    }

    public void recomputeArmorClass() {
        float itemsArmorClass = 0.0f;
        float itemsArmorPenalty = 0.0f;
        float itemsMovementPenalty = 0.0f;
        float itemsShieldAC = 0.0f;

        EquippableItem offItem = parent.inventory.getEquippedItem(Slot.OffHand);
        Weapon offWeapon = offItem != null && offItem.getTemplate().getType() == EquippableItemTemplate.Type.Weapon ?
                (Weapon) offItem : null;

        List<Armor> armorItems = new ArrayList<>(5);
        armorItems.add((Armor) parent.inventory.getEquippedItem(Slot.Armor));
        armorItems.add((Armor) parent.inventory.getEquippedItem(Slot.Helmet));
        armorItems.add((Armor) parent.inventory.getEquippedItem(Slot.Gloves));
        armorItems.add((Armor) parent.inventory.getEquippedItem(Slot.Boots));

        if (offItem != null && offItem.getTemplate().getType() == EquippableItemTemplate.Type.Shield)
            armorItems.add((Armor) parent.inventory.getEquippedItem(Slot.OffHand));

        for (Armor armor : armorItems) {
            if (armor == null) continue;

            switch (armor.getTemplate().getType()) {
                case Shield:
                    itemsShieldAC += armor.getQualityModifiedArmorClass() *
                            (100.0f + get(armor.getTemplate().getArmorType().getName(), Type.ArmorTypeArmorClass)) / 100.0f;
                default:
                    itemsArmorClass += armor.getQualityModifiedArmorClass() *
                            (100.0f + get(armor.getTemplate().getArmorType().getName(), Type.ArmorTypeArmorClass)) / 100.0f;
                    itemsArmorPenalty += armor.getQualityModifiedArmorPenalty() *
                            (100.0f - get(armor.getTemplate().getArmorType().getName(), Type.ArmorTypeArmorPenalty)) / 100.0f;
                    itemsMovementPenalty += armor.getQualityModifiedMovementPenalty() *
                            (100.0f - get(armor.getTemplate().getArmorType().getName(), Type.ArmorTypeMovementPenalty)) / 100.0f;
            }
        }
        // if a shield was present, we double counted shield AC bonus
        itemsArmorClass -= itemsShieldAC;

        if (offWeapon != null) itemsShieldAC = get(Type.DualWieldArmorClass);

        zeroStats(Stat.ArmorClass, Stat.TouchArmorClass, Stat.ArmorPenalty, Stat.MovementBonus);
        zeroStats(Stat.MovementCost, Stat.InitiativeBonus);

        addToStat(Stat.InitiativeBonus, get(Type.Initiative));
        addToStat(Stat.ArmorPenalty, get(Type.ArmorPenalty) + (int) itemsArmorPenalty);

        // immobilization immunity prevents being slowed down as well
        int baseMovementBonus = get(Type.Movement);
        if (has(Type.ImmobilizationImmunity) && baseMovementBonus < 0)
            baseMovementBonus = 0;

        addToStat(Stat.MovementBonus, Math.min(100, baseMovementBonus - (int) itemsMovementPenalty));

        addToStat(Stat.MovementCost, parent.getTemplate().getRace().getMovementCost() * (100 - get(Stat.MovementBonus)) / 100);

        int deflection = get(Type.ArmorClass, StackType.DeflectionBonus, StackType.DeflectionPenalty);
        int naturalArmor = get(Type.ArmorClass, StackType.NaturalArmorBonus, StackType.NaturalArmorPenalty);
        int armor = get(Type.ArmorClass, StackType.ArmorBonus, StackType.ArmorPenalty);
        int shield = get(Type.ArmorClass, StackType.ShieldBonus, StackType.ShieldPenalty);

        int dodgeAC = get(Type.ArmorClass, StackType.StackableBonus, StackType.StackablePenalty);

        float armorModifier = (100.0f - get(Stat.ArmorPenalty)) / 100.0f;

        // if dex AC value is positive, it is decreased by the armor modifier
        // if it is negative, apply the full amount
        float dexACValue = 3.0f * (getDex() - 10.0f);
        int dexACBonus = 0;
        if (dexACValue > 0.0) dexACBonus = (int) (dexACValue * armorModifier);
        else dexACBonus = (int) dexACValue;

        armor = Math.max(armor, (int) itemsArmorClass);
        shield = Math.max(shield, (int) itemsShieldAC);

        addToStat(Stat.ArmorClass, 50 + deflection + naturalArmor + armor + shield);
        addToStat(Stat.TouchArmorClass, 50 + deflection + shield);

        if (parent.stats.isImmobilized()) {
            addToStat(Stat.ArmorClass, -20);
            addToStat(Stat.TouchArmorClass, -20);
        } else {
            addToStat(Stat.ArmorClass, dodgeAC + dexACBonus);
            addToStat(Stat.TouchArmorClass, dodgeAC + dexACBonus);
            addToStat(Stat.InitiativeBonus, (getDex() - 10) * 2);
        }
    }

    public void recomputeAttackBonus() {
        Weapon mainWeapon = parent.getMainHandWeapon();

        EquippableItem offItem = parent.inventory.getEquippedItem(Slot.OffHand);
        Weapon offWeapon = offItem != null && offItem.getTemplate().getType() == EquippableItemTemplate.Type.Weapon ?
                (Weapon) offItem : null;

        zeroStats(Stat.MainHandAttackBonus, Stat.MainHandDamageBonus, Stat.OffHandAttackBonus, Stat.OffHandDamageBonus);
        zeroStats(Stat.AttackCost, Stat.TouchAttackBonus, Stat.ShieldAttackPenalty);

        if (offItem != null && offItem.getTemplate().getType() == EquippableItemTemplate.Type.Shield) {
            addToStat(Stat.ShieldAttackPenalty,
                    Math.min(0, get(Type.ShieldAttack) - ((Armor) offItem).getTemplate().getShieldAttackPenalty()));
        }

        String mainBaseWeapon = mainWeapon.getTemplate().getBaseWeapon().getName();

        addToStat(Stat.MainHandAttackBonus, get(Type.MainHandAttack) + get(Type.Attack));
        addToStat(Stat.MainHandDamageBonus, get(Type.MainHandDamage) + get(Type.Damage));
        addToStat(Stat.MainHandAttackBonus, get(mainBaseWeapon, Type.BaseWeaponAttack));
        addToStat(Stat.MainHandDamageBonus, get(mainBaseWeapon, Type.BaseWeaponDamage));
        addToStat(Stat.MainHandAttackBonus, get(Stat.ShieldAttackPenalty));

        switch (mainWeapon.getTemplate().getWeaponType()) {
            case Melee:
                switch (mainWeapon.getTemplate().getHanded()) {
                    case Light:
                        addToStat(Stat.MainHandAttackBonus, (Math.max(getDex(), getStr()) - 10) * 2);
                        addToStat(Stat.MainHandDamageBonus, get(Type.LightMeleeWeaponDamage));
                        addToStat(Stat.MainHandAttackBonus, get(Type.LightMeleeWeaponAttack));
                        break;
                    case OneHanded:
                        addToStat(Stat.MainHandAttackBonus, get(Type.OneHandedMeleeWeaponAttack) + (getStr() - 10) * 2);
                        addToStat(Stat.MainHandDamageBonus, get(Type.OneHandedMeleeWeaponDamage));
                        break;
                    case TwoHanded:
                        addToStat(Stat.MainHandAttackBonus, get(Type.TwoHandedMeleeWeaponAttack) + (getStr() - 10) * 2);
                        addToStat(Stat.MainHandDamageBonus, get(Type.TwoHandedMeleeWeaponDamage));
                }

                // add extra damage bonus for not dual wielding
                if (offWeapon == null) addToStat(Stat.MainHandDamageBonus, 8 * (getStr() - 10));

                break;
            default:
                addToStat(Stat.MainHandAttackBonus,
                        (getDex() - 10) * 2 + get(Type.RangedAttack));
                addToStat(Stat.MainHandDamageBonus,
                        Math.max(mainWeapon.getTemplate().getMinStrengthBonus(),
                                Math.min((getStr() - 10) * 8, mainWeapon.getTemplate().getMaxStrengthBonus())));
                addToStat(Stat.MainHandDamageBonus,
                        get(Type.RangedDamage));
        }

        int mainWeaponSpeedBonus = get(mainBaseWeapon, Type.BaseWeaponSpeed) + get(Type.AttackCost);
        int mainWeaponSpeed = mainWeapon.getTemplate().getAttackCost() * (100 - mainWeaponSpeedBonus) / 100;
        int offWeaponSpeed = 0;

        if (offWeapon != null) {
            String offBaseWeapon = offWeapon.getTemplate().getBaseWeapon().getName();

            addToStat(Stat.OffHandAttackBonus, get(Type.OffHandAttack) + get(Type.Attack));
            addToStat(Stat.OffHandDamageBonus, get(Type.OffHandDamage) + get(Type.Damage));
            addToStat(Stat.OffHandAttackBonus, get(offBaseWeapon, Type.BaseWeaponAttack));
            addToStat(Stat.OffHandDamageBonus, get(offBaseWeapon, Type.BaseWeaponDamage));

            int offWeaponSpeedBonus = get(offBaseWeapon, Type.BaseWeaponSpeed) + get(Type.AttackCost);
            offWeaponSpeed = offWeapon.getTemplate().getAttackCost() * (100 - offWeaponSpeedBonus) / 100;

            int offWeaponLightBonus = 0;

            switch (offWeapon.getTemplate().getHanded()) {
                case Light:
                    addToStat(Stat.OffHandAttackBonus, (getDex() - 10) * 2 + get(Type.LightMeleeWeaponAttack));
                    addToStat(Stat.OffHandDamageBonus, get(Type.LightMeleeWeaponDamage));
                    break;
                case OneHanded:
                    addToStat(Stat.OffHandAttackBonus, (getStr() - 10) * 2 + get(Type.OneHandedMeleeWeaponAttack));
                    addToStat(Stat.OffHandDamageBonus, get(Type.OneHandedMeleeWeaponDamage));
                    offWeaponLightBonus = -10;
                    break;
                default:
            }

            addToStat(Stat.OffHandDamageBonus, (3 + get(Type.DualWieldStrDamage)) * (getStr() - 10));
            addToStat(Stat.MainHandDamageBonus, (5 + get(Type.DualWieldStrDamage)) * (getStr() - 10));

            addToStat(Stat.MainHandAttackBonus, get(Type.DualWieldAttack) - 15 + offWeaponLightBonus);
            addToStat(Stat.OffHandAttackBonus, get(Type.DualWieldAttack) - 25 + offWeaponLightBonus);
        }

        addToStat(Stat.AttackCost, Math.max(mainWeaponSpeed, offWeaponSpeed));

        addToStat(Stat.TouchAttackBonus, (getDex() - 10) * 2 + get(Type.Attack));
    }

    public void recomputeAllStats() {
        bonuses.clear();

        synchronized (parent.getEffects()) {
            for (Effect effect : parent.getEffects()) {
                addAllNoRecompute(effect.getBonuses());
            }
        }

        for (Slot slot : Slot.values()) {
            EquippableItem item = parent.inventory.getEquippedItem(slot);
            if (item == null) continue;

            addAllNoRecompute(item.getBonusList());
        }

        recomputeStrNoAttackBonus();
        recomputeDex();
        recomputeCon();
        recomputeInt();
        recomputeWis();
        recomputeCha();
    }

    public void setStat(String stat, int value) {
        setStat(Stat.valueOf(stat), value);
    }

    public void setStat(Stat stat, int value) {
        stats.put(stat, value);

        recomputeAllStats();
    }

    public int[] getAttributes() {
        int[] attributes = new int[6];

        attributes[0] = stats.get(Stat.BaseStr);
        attributes[1] = stats.get(Stat.BaseDex);
        attributes[2] = stats.get(Stat.BaseCon);
        attributes[3] = stats.get(Stat.BaseInt);
        attributes[4] = stats.get(Stat.BaseWis);
        attributes[5] = stats.get(Stat.BaseCha);

        return attributes;
    }

    public void setAttributes(int[] attributes) {
        if (attributes == null) return;

        if (attributes.length == 6) {
            stats.put(Stat.BaseStr, attributes[0]);
            stats.put(Stat.BaseDex, attributes[1]);
            stats.put(Stat.BaseCon, attributes[2]);
            stats.put(Stat.BaseInt, attributes[3]);
            stats.put(Stat.BaseWis, attributes[4]);
            stats.put(Stat.BaseCha, attributes[5]);
        }
    }

    public int getBaseSpellFailure(int spellLevel) {
        Role role = parent.roles.getBaseRole();

        int abilityScore = get(role.getSpellCastingAttribute());
        int casterLevel = getCasterLevel();

        int failure = role.getSpellFailureBase() + role.getSpellFailureSpellLevelFactor() * spellLevel;
        failure -= (abilityScore - 10) * role.getSpellFailureAbilityScoreFactor();
        failure -= casterLevel * role.getSpellFailureCasterLevelFactor();
        failure -= get(Type.SpellFailure);

        return failure;
    }

    public int getCasterLevel() {
        return get(Stat.CasterLevel);
    }

    public int getCreatureLevel() {
        return get(Stat.CreatureLevel);
    }

    public int getLevelDamageBonus() {
        return get(Stat.LevelDamageBonus);
    }

    public int getLevelAttackBonus() {
        return get(Stat.LevelAttackBonus);
    }

    public int getMaxHP() {
        return get(Stat.MaxHP);
    }

    public int getSpellCastingAttribute() {
        return get(parent.roles.getBaseRole().getSpellCastingAttribute());
    }

    public int getBaseStr() {
        return get(Stat.BaseStr) + get(Type.BaseStr);
    }

    public int getBaseDex() {
        return get(Stat.BaseDex) + get(Type.BaseDex);
    }

    public int getBaseCon() {
        return get(Stat.BaseCon) + get(Type.BaseCon);
    }

    public int getBaseInt() {
        return get(Stat.BaseInt) + get(Type.BaseInt);
    }

    public int getBaseWis() {
        return get(Stat.BaseWis) + get(Type.BaseWis);
    }

    public int getBaseCha() {
        return get(Stat.BaseCha) + get(Type.BaseCha);
    }

    public int getStr() {
        return get(Stat.Str);
    }

    public int getDex() {
        return get(Stat.Dex);
    }

    public int getCon() {
        return get(Stat.Con);
    }

    public int getInt() {
        return get(Stat.Int);
    }

    public int getWis() {
        return get(Stat.Wis);
    }

    public int getCha() {
        return get(Stat.Cha);
    }

    public int getMentalResistance() {
        return get(Stat.MentalResistance) + get(Type.MentalResistance);
    }

    public int getPhysicalResistance() {
        return get(Stat.PhysicalResistance) + get(Type.PhysicalResistance);
    }

    public int getReflexResistance() {
        return get(Stat.ReflexResistance) + get(Type.ReflexResistance);
    }

    public int getWeightLimit() {
        return get(Stat.WeightLimit) + get(Type.WeightLimit) * 1000;
    }

    public boolean isHidden() {
        return bonuses.has(Type.Hidden);
    }

    public boolean isImmobilized() {
        return (bonuses.has(Type.Immobilized) || bonuses.has(Type.UndispellableImmobilized))
                && !bonuses.has(Type.ImmobilizationImmunity);
    }

    public boolean isHelpless() {
        return (bonuses.has(Type.Helpless) || bonuses.has(Type.UndispellableHelpless))
                && !bonuses.has(Type.ImmobilizationImmunity);
    }

    public int getAttacksOfOpportunity() {
        return 1 + get(Type.AttacksOfOpportunity);
    }

    public int getAttackCost() {
        return stats.get(Stat.AttackCost);
    }

    public int getMovementCost() {
        return stats.get(Stat.MovementCost);
    }

    public boolean getMentalResistanceCheck(int difficulty) {
        return getCheck(difficulty, getMentalResistance(), new StringBuilder("Mental Resistance Check: "));
    }

    public boolean getPhysicalResistanceCheck(int difficulty) {
        return getCheck(difficulty, getPhysicalResistance(), new StringBuilder("Physical Resistance Check: "));
    }

    public boolean getReflexResistanceCheck(int difficulty) {
        return getCheck(difficulty, getReflexResistance(), new StringBuilder("Reflex Resistance Check: "));
    }

    private boolean getCheck(int difficulty, int resistance, StringBuilder sb) {
        int roll = Game.dice.d100();
        int total = resistance + roll;

        sb.insert(0, parent.getTemplate().getName() + " attempts ");

        sb.append(resistance);
        sb.append(" + ");
        sb.append(roll);
        sb.append(" = ");
        sb.append(total);
        sb.append(" vs ");
        sb.append(difficulty);
        sb.append(" : ");

        if (total >= difficulty) {
            sb.append("Success.");
            Game.mainViewer.addMessage("orange", sb.toString());
            return true;
        } else {
            sb.append("Failure.");
            Game.mainViewer.addMessage("orange", sb.toString());
            return false;
        }
    }

    private enum RecomputeMode {
        Removal, Addition
    }
}

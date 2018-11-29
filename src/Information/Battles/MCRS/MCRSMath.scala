package Information.Battles.MCRS

import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object MCRSMath {

	// McRave's tuning: https://docs.google.com/spreadsheets/d/15_2BlFi27EguWciAGbWKCxLLacCnh9QFl1zSkc799Xo/edit#gid=135581064
	
	def survivability(unit: UnitInfo): Double = {
		val speed = if (unit.unitClass.isBuilding) 0.5 else Math.max(1.0, Math.log(unit.topSpeed))
		val armor = 2.0 + unit.armorHealth
		val health = Math.log(unit.unitClass.maxTotalHealth / 20.0)
		speed * armor * health
	}

	def splashModifier(unit: UnitInfo): Double = {
		if (unit.is(Protoss.Archon)) return 1.25
    if (unit.is(Terran.Firebat)) return 1.25
		if (unit.is(Protoss.Reaver)) return 1.25
		if (unit.is(Protoss.HighTemplar)) return 6
		if (unit.is(Terran.SiegeTankSieged)) return 2.5
		if (unit.is(Terran.Valkyrie)) return 1.5
    if (unit.is(Zerg.Mutalisk)) return 1.5
		if (unit.is(Zerg.Lurker)) return 2
		1
	}

	def groundDamage(unit: UnitInfo): Double = {
		if (unit.is(Protoss.HighTemplar)) return 112
    unit.damageOnHitGround
	}
	def airDamage(unit: UnitInfo): Double = {
		if (unit.is(Protoss.HighTemplar)) return 112
		unit.damageOnHitAir
	}

	def groundRange(unit: UnitInfo): Double = {
    if (unit.is(Protoss.HighTemplar)) return 288
    unit.pixelRangeGround
	}
	def airRange(unit: UnitInfo): Double = {
		if (unit.is(Protoss.HighTemplar)) return 288
		unit.pixelRangeAir
	}

	def gWeaponCooldown(unit: UnitInfo): Double = {
		if (unit.is(Protoss.HighTemplar)) return 224.0
		if (unit.is(Zerg.InfestedTerran)) return 500
		unit.cooldownMaxGround
	}
	def aWeaponCooldown(unit: UnitInfo): Double = {
    if (unit.is(Protoss.HighTemplar)) return 224
    if (unit.is(Zerg.Scourge)) return 110
		unit.cooldownMaxAir
	}

	def groundDPS(unit: UnitInfo): Double = {
		val damage = groundDamage(unit)
		if (damage <= 0) return 0.0
		(splashModifier(unit)
			* splashModifier(unit)
			* groundDamage(unit)
			* Math.log(unit.pixelRangeGround)
			/ gWeaponCooldown(unit))
	}
	def airDPS(unit: UnitInfo): Double = {
		val damage = airDamage(unit)
		if (damage <= 0) return 0.0
		(splashModifier(unit)
			* airDamage (unit)
			* Math.log(unit.pixelRangeAir)
			/ aWeaponCooldown(unit))
	}

	def visGroundStrength(unit: UnitInfo): Double = {
		if (!unit.canDoAnything) return 0
		unit.mcrs.percentHealth * unit.mcrs.maxGroundStrength()
	}
	def visAirStrength(unit: UnitInfo): Double = {
    if (!unit.canDoAnything) return 0
		unit.mcrs.percentHealth * unit.mcrs.maxAirStrength()
	}

	def maxGroundStrength(unit: UnitInfo): Double = {
		if (unit.is(Terran.Medic)) return 5.0
		if (unit.isAny(Protoss.Scarab, Terran.SpiderMine, Zerg.Egg, Zerg.Larva)) return 0
    if (unit.pixelRangeGround <= 0) return 0
		if (unit.is(Protoss.Interceptor)) return 4 // Originally 2 with Carrier-specific weight
		groundDPS(unit) * Math.log(survivability(unit))
	}
	def maxAirStrength(unit: UnitInfo): Double = {
		if (unit.isAny(Protoss.Scarab, Terran.SpiderMine, Zerg.Egg, Zerg.Larva)) return 0
    if (unit.pixelRangeAir <= 0) return 0
		if (unit.is(Protoss.Interceptor)) return 4 // Originally 2 with Carrier-specific weight
		airDPS(unit) * Math.log(survivability(unit))
	}

	def percentHealth(unit: UnitInfo): Double = {
    val shieldRatio = 0.5
		(unit.hitPoints + shieldRatio * unit.shieldPoints) / (unit.unitClass.maxHitPoints.toDouble + shieldRatio * unit.unitClass.maxShields)
	}
}

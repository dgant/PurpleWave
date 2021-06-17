package Information.Battles.MCRS

import Mathematics.Maff
import ProxyBwapi.Engine.Size
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object MCRSMath {

	def survivability(unit: UnitInfo): Double = {
		val damageIn = unit.team.map(_.opponent.meanDamageAgainst(unit)).getOrElse(0.0)
		val armor = if (unit.totalHealth > 0) (unit.armorHealth * unit.hitPoints + unit.armorShield * unit.shieldPoints) / unit.totalHealth else 0
		val armorBonus = 1.0 + Maff.nanToOne(Math.max(0.5, damageIn - armor.toDouble) / damageIn)
		val typeBonus = if (unit.unitClass.size == Size.Small || unit.unitClass.size == Size.Large) 1.3 else 1.0 // TODO: Base on threat damage type
		armorBonus * typeBonus * unit.unitClass.maxTotalHealth
	}

	def splashModifier(unit: UnitInfo): Double = unit.matchups.splashFactorMax

	def damage(unit: UnitInfo, flying: Boolean): Double = {
		if (unit.is(Protoss.HighTemplar)) return 112
		unit.mcrs.target()
  		.filter(_.flying == flying)
			.map(t => unit.damageOnNextHitAgainst(t, from = Some(unit.mcrs.engagePosition())))
			.getOrElse(if (flying) unit.damageOnHitAir else unit.damageOnHitGround)
  		.toDouble
	}

	def groundRange(unit: UnitInfo): Double = {
    if (unit.is(Protoss.HighTemplar)) return 288
    unit.pixelRangeGround
	}
	def airRange(unit: UnitInfo): Double = {
		if (unit.is(Protoss.HighTemplar)) return 288
		unit.pixelRangeAir
	}

	def cooldownGround(unit: UnitInfo): Double = {
		if (unit.is(Protoss.HighTemplar)) return 224.0
		if (unit.is(Zerg.InfestedTerran)) return 500
		unit.cooldownMaxGround
	}
	def cooldownAir(unit: UnitInfo): Double = {
    if (unit.is(Protoss.HighTemplar)) return 224
    if (unit.is(Zerg.Scourge)) return 110
		unit.cooldownMaxAir
	}

	def dpfGround(unit: UnitInfo): Double = {
		val damageNext = damage(unit, flying = false)
		if (damageNext <= 0) return 0.0
		unit.matchups.splashFactorMax * damageNext / cooldownGround(unit)
	}
	def dpfAir(unit: UnitInfo): Double = {
		val damageNext = damage(unit, flying = true)
		if (damageNext<= 0) return 0.0
		unit.matchups.splashFactorMax * damageNext / cooldownAir(unit)
	}

	def strengthGround(unit: UnitInfo): Double = {
		if (!unit.canDoAnything) return 0
		unit.mcrs.percentHealth * maxGroundStrength(unit)
	}
	def strengthAir(unit: UnitInfo): Double = {
    if (!unit.canDoAnything) return 0
		unit.mcrs.percentHealth * maxAirStrength(unit)
	}

	def maxGroundStrength(unit: UnitInfo): Double = {
		if (unit.attacksAgainstGround == 0) return 0
		if (unit.is(Terran.Medic)) return 5.0
		if (unit.isAny(Protoss.Scarab, Terran.SpiderMine, Zerg.Egg, Zerg.Larva)) return 0
		if (unit.is(Protoss.Interceptor)) return 4 // Originally 2 with Carrier-specific weight
		unit.mcrs.dpsGround() * unit.mcrs.survivability()
	}
	def maxAirStrength(unit: UnitInfo): Double = {
		if (unit.attacksAgainstAir == 0) return 0
		if (unit.isAny(Protoss.Scarab, Terran.SpiderMine, Zerg.Egg, Zerg.Larva)) return 0

		if (unit.is(Protoss.Interceptor)) return 4 // Originally 2 with Carrier-specific weight
		unit.mcrs.dpsAir() * unit.mcrs.survivability()
	}

	def percentHealth(unit: UnitInfo): Double = {
    val shieldRatio = 0.5
		(unit.hitPoints + shieldRatio * unit.shieldPoints) / (unit.unitClass.maxHitPoints.toDouble + shieldRatio * unit.unitClass.maxShields)
	}
}

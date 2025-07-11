package Micro.Targeting

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{CombatUnit, FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.UnitFilters.IsAny

object TargetScoring {

  @inline private def pixelCost(attacker: CombatUnit, target: CombatUnit): Double = {
    Math.max(0, attacker.pixelDistanceCenter(target) - attacker.pixelRangeAgainst(target) - attacker.unitClass.dimensionMin - target.unitClass.dimensionMin - 0.5 * attacker.cooldownLeft * attacker.topSpeed)
  }

  @inline private def attackValue(targetValue: Double, injury: Double, pixelCost: Double, efficacy: Double): Double = {
    val value = targetValue + 4.0 * injury
    ?(value > 0, efficacy, 1.0) * value - Maff.inv32 * pixelCost
  }

  /**
    * Scores the value of an (attacker, target) pair. Uses a fast formula, for use in combat sim.
    * Scores are denominated in tiles.
    */
  @inline def fast(attacker: CombatUnit, target: CombatUnit): Double = {
    attackValue(
      targetValue = target.targetValue,
      injury      = target.injury,
      pixelCost   = pixelCost(attacker, target),
      efficacy    = attacker.damageMultiplierAgainst(target) * attacker.hitChanceAgainst(target))
  }

  /**
    * Scores the value of an (attacker, target) pair. Uses a slow formula which accounts for more factors.
    * Scores are denominated in tiles.
    */
  @inline def slow(attacker: FriendlyUnitInfo, target: UnitInfo): Double = {
    val splashEfficacy = mult(
      Math.max(1.0, 0.5 * target.tile.adjacent9.iterator.map(_.units.count(u => attacker.canAttack(u) && u.isEnemy)).sum),
      attacker.isAny(Terran.SiegeTankSieged, Protoss.Archon, Protoss.Reaver, Zerg.Lurker))

    val attackValue = this.attackValue(
      targetValue = target.targetValue,
      injury      = ?(target.doomed, 0.0, target.injury),
      pixelCost   = pixelCost(attacker, target),
      efficacy    = splashEfficacy * attacker.hitChanceAgainst(target) * attacker.damageMultiplierAgainst(target))

    val active      = target.canAttack
    val passive     = ! target.unitClass.attacksOrCastsOrDetectsOrTransports
    val inCombat    = attacker.team.exists(_.engagedUpon) || target.team.exists(_.engagingOn)
    val actionMatch = (inCombat && active) || ( ! inCombat && passive)
    val combatValue = combatBonus * ?(actionMatch, 1, -1)

    var meleeSpreadBonus = 8.0
    if (attacker.unitClass.melee) {
      val otherAttackers  = Math.max(0, target.targetedByRecentlyMelee.size - 1)
      val surfaceArea     = target.unitClass.perimeter
      val surfaceTaken    = attacker.unitClass.dimensionMax * otherAttackers
      val surfaceRatio    = surfaceTaken / surfaceArea
      if (otherAttackers > 1 && surfaceRatio >= 0.25) {
        meleeSpreadBonus = Math.max(0, meleeSpreadBonus - 1.0 * otherAttackers)
      }
    }

    val catchBonus = add(12.0, target.caught || target.caughtBy(attacker))

    val threatCost = ?(Protoss.Carrier(attacker), target.tile, attacker.pixelToFireAtSimple(target).tile).enemiesAttacking(attacker).length match {
      case 0 => 0.0
      case 1 => 1.0
      case _ => 3.0
    }

    (attackValue + combatValue + meleeSpreadBonus + catchBonus) * splashEfficacy - threatCost
  }

  /**
    * Measures the value of targeting a unit, in isolation
    * The unit of value is tiles, as in, how far would we extend ourselves to attack this unit?
    */
  private lazy val baseValue = 1.0 / Protoss.Dragoon.subjectiveValueOverHealth
  private lazy val cloakyMatcher = IsAny(Zerg.Lurker, Protoss.Arbiter, Protoss.DarkTemplar)
  private val combatBonus = 8.0
  def apply(target: UnitInfo): Double = {
    var output = 4.0 * baseValue * target.unitClass.subjectiveValueOverHealth

    output += add(10.0, target.matrixPoints == 0)

    // Immobility bonus
    output += add(4.0, target.constructing)
    output += add(6.0, target.gathering)

    if (target.unitClass.attacksOrCastsOrDetectsOrTransports && ! Zerg.Overlord(target)) {
      // Combat bonus; granted by default to aid simulation, but removed selectively
      output += combatBonus

      // Protect our fliers
      // Disabled because use of presumptiveTarget() was causing NPEs in the combat sim thread, though we did seem to have a solution for this prior to introducing presumptiveTarget
      //output += add(2.0, target.presumptiveTarget.exists(t => t.isEnemyOf(target) && t.flying && t.attacksAgainstGround > 0))

      // Goose chase penalties
      output += add(2.0, ! target.canMove)
      output += add(6.0, ! target.canMove || ?(target.isOurs, target.visibleToOpponents, target.visible))
      output += add(6.0, ! target.canMove || target.likelyStillThere)
    }

    // Doom penalties
    output += add(2.0, ! target.likelyDoomed)
    output += add(2.0, ! target.doomed)

    // Cloaked bonus: Target cloaked units before they escape detection
    // modified because it needs performant player-agnosticism for simulation
    //output += add(8.0, target.cloaked && target.detected && ! With.units.existsEnemy(Protoss.Arbiter))
    output += add(8.0, Protoss.DarkTemplar(target))

    // Burrow bonus: Target burrowed units while scan is active
    // Disabled because it needs performant player-agnosticism for simulation
    //output += add(8.0, (target.burrowed && With.units.existsOurs(Terran.SpellScannerSweep)))

    // Detector bonus
    output += add(6.0,
      ?(
        target.isEnemy,
        With.units.existsOurs(cloakyMatcher),
        With.units.existsEnemy(cloakyMatcher))
      && aidsDetection(target))

    // Alternative repair value
    output = Math.max(output, target.orderTarget.filter(target.repairing && ! _.repairing).map(2.5 + _.targetValue).getOrElse(0.0))

    output
  }

  def aidsDetection(target: UnitInfo): Boolean = {
    var output = false
    output ||= target.unitClass.isDetector
    output ||= Terran.Comsat(target)
    output ||= Terran.Academy(target)           && ! With.units.existsEnemy(Terran.Comsat, Terran.ScienceVessel, Terran.ScienceFacility)
    output ||= Terran.EngineeringBay(target)    && ! With.units.existsEnemy(Terran.Comsat, Terran.ScienceVessel, Terran.ScienceFacility, Terran.MissileTurret)
    output ||= Protoss.RoboticsFacility(target) && ! With.units.existsEnemy(Protoss.Observer)
    output ||= Protoss.Forge(target)            && ! With.units.existsEnemy(Protoss.Observer, Protoss.Observatory, Protoss.PhotonCannon)
    output ||= Zerg.CreepColony(target)         && With.units.existsEverEnemy(Zerg.SporeColony, Zerg.EvolutionChamber)
    output
  }

  @inline private def add(value: => Double, predicate: Boolean): Double = {
    ?(predicate, value, 0.0)
  }
  @inline private def mult(value: => Double, predicate: Boolean): Double = {
    ?(predicate, value, 1.0)
  }
}

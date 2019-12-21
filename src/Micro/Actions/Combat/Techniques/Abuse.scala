package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.Activators.WeightedMin
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, PotshotAsSoonAsPossible}
import Micro.Actions.Commands.Attack
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Abuse extends ActionTechnique {
  
  // If we outrange and out-speed our enemies,
  // we can painlessly kill them as long as we maintain the gap and don't get cornered.
  // eg. Dragoons vs. slow Zealots
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.canFight
    && unit.unitClass.ranged
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.matchups.threats.nonEmpty
    && ! unit.isAny(Protoss.Reaver, Protoss.Corsair, Zerg.Lurker)
  )
  
  override val activator = new WeightedMin(this)

  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    lazy val defenders        = unit.matchups.threats.view.filter(t => t.matchups.threats.isEmpty || t.pixelRangeAgainst(unit) >= unit.effectiveRangePixels)
    lazy val targets          = unit.matchups.targets.view.filter(t => ! t.canAttack(unit) || unit.pixelRangeAgainst(t) > t.pixelRangeAgainst(unit))
    lazy val defenderNearest  = ByOption.minBy(defenders)(_.pixelDistanceEdge(unit))
    lazy val targetNearest    = ByOption.minBy(targets)(_.pixelDistanceEdge(unit))

    if (targetNearest.isEmpty) return 0.0
    if (defenderNearest.isEmpty) return 1.0
    if (ByOption.maxBy(unit.matchups.framesOfEntanglementPerThreat)(_._2).map(_._1).forall(threat =>
      unit.canAttack(threat)
      && unit.inRangeToAttack(threat)
      && threat.pixelRangeAgainst(unit) + 32 * 2 < unit.pixelRangeAgainst(threat))) return 1.0

    val target = targetNearest.get
    val defender = defenderNearest.get

    val pixelsOutsideRangeUs      = unit.pixelDistanceEdge(defender)  - defender.pixelRangeAgainst(unit)
    val pixelsOutsideRangeTarget  = unit.pixelDistanceEdge(target)    - unit.pixelRangeAgainst(target)
    val output = if (pixelsOutsideRangeUs > 32 + pixelsOutsideRangeTarget) 1.0 else 0.0
    output
  }
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if ( ! other.canAttack(unit)) return None
    if ( ! unit.canAttack(other)) return None
    if (other.unitClass.isWorker && other.pixelDistanceEdge(unit) > 32) return None

    lazy val ourSpeed     = Math.max(unit.topSpeed, unit.agent.ride.map(_.topSpeed).getOrElse(0.0))
    lazy val deltaThreats = other.matchups.threats.size - unit.matchups.threats.size
    lazy val deltaSpeed   = ourSpeed - other.topSpeed
    lazy val canOle = (
      deltaSpeed == 0.0
        && deltaThreats > 0
        && ! other.inRangeToAttack(unit)
        && ! other.inRangeToAttack(unit, unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate))
    
    if (deltaSpeed <= 0 && ! canOle) return Some(0.0)
  
    val deltaRange = unit.pixelRangeAgainst(other) - other.pixelRangeAgainst(unit)
    if (deltaRange <= 0) return Some(0.0)
    
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val safetyFrames = unit.unitClass.framesToTurnAndShootAndTurnBackAndAccelerate
    lazy val safeToShoot = (
      unit.matchups.framesOfSafety > safetyFrames
      || unit.matchups.framesOfEntanglementPerThreat.forall(threatEntanglement =>
        threatEntanglement._2 > safetyFrames
        // Handles a case where rangeless Goons couldn't get in range to attack a stuck Zealot
        || ( ! threatEntanglement._1.inRangeToAttack(unit) && ! threatEntanglement._1.moving && ! threatEntanglement._1.presumptiveTarget.contains(unit))
        || threatEntanglement._1.orderTarget.exists(_ != unit)
        || threatEntanglement._1.matchups.targets.exists(ally =>
          threatEntanglement._1.pixelDistanceEdge(ally) <
          threatEntanglement._1.pixelDistanceEdge(unit) - 32)
      ))
    lazy val lastChanceToShoot = unit.matchups.targetsInRange.isEmpty || unit.matchups.targets.forall(t => t.pixelDistanceEdge(unit) > unit.pixelRangeAgainst(t) - 32.0)
    if (unit.readyForAttackOrder && (safeToShoot || lastChanceToShoot)) {
      Potshot.delegate(unit)
      PotshotAsSoonAsPossible.delegate(unit)
      Attack.delegate(unit)
      if (unit.agent.toAttack.isEmpty) return
    }
    if (unit.matchups.framesOfSafety < GameTime(0, 1)()) {
      Avoid.delegate(unit)
    }
  }
  
}

package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Targeting.Target
import Micro.Agency.Commander
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Brawl extends Action {
  
  // In close-quarters fights against other melee units,
  // prefer non-targeted commands to avoid glitching.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val brawlers = unit.matchups.threats.filter(t => ! t.flying && t.unitClass.melee && inBrawlRange(unit, t))
    (unit.agent.shouldEngage
      && unit.canMove
      && (unit.is(Zerg.Zergling) || (unit.unitClass.melee && unit.matchups.threats.exists(Zerg.Zergling)))
      && unit.matchups.targets.exists(t => ! t.flying && t.unitClass.melee)
      && unit.matchups.threats.exists(t => ! t.flying && t.unitClass.melee && t.pixelDistanceEdge(unit) < 32.0)
      && unit.matchups.targetsInRange.forall(_.canAttack(unit))
      && unit.matchups.targetsInRange.exists(_.canAttack(unit))
      && brawlers.size >= 4)
  }
  
  private def inBrawlRange(unit: FriendlyUnitInfo, other: UnitInfo): Boolean = {
    val framesBeforeContact = unit.pixelDistanceEdge(other) / (unit.topSpeed + other.topSpeed)
    val framesBeforeReacting = With.reaction.agencyMax + unit.unitClass.framesToTurn180
    framesBeforeReacting >= framesBeforeContact
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val framesClosest = ByOption.min(unit.matchups.targets.map(unit.framesBeforeAttacking))
    lazy val targetsNear = unit.matchups.targets.filter(t => framesClosest.exists(f => unit.framesBeforeAttacking(t) <= f))
    unit.agent.toAttack =
      Target.best(unit, unit.matchups.targetsInRange)
        .orElse(Target.best(unit, targetsNear))
        .orElse(Target.best(unit))
    Commander.attack(unit)
  }
}

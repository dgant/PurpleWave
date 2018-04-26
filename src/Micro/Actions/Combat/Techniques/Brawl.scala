package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Attacking.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Attacking.{Target, TargetAction, TargetInRange}
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Brawl extends ActionTechnique {
  
  // In close-quarters fights against other melee units,
  // prefer non-targeted commands to avoid glitching.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && ! unit.unitClass.isWorker
    && unit.matchups.targets.exists(t => ! t.flying && t.unitClass.melee)
    && unit.matchups.threats.exists(t => ! t.flying && t.unitClass.melee && t.pixelDistanceEdge(unit) < 32.0)
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    lazy val brawlers = unit.matchups.threats.filter(t => ! t.flying && t.unitClass.melee && inBrawlRange(unit, t))
    if (unit.unitClass.melee && brawlers.size >= 4) 1.0 else 0.0
  }
  
  private def inBrawlRange(unit: FriendlyUnitInfo, other: UnitInfo): Boolean = {
    val framesBeforeContact = unit.pixelDistanceEdge(other) / (unit.topSpeed + other.topSpeed)
    val framesBeforeReacting = With.reaction.agencyMax + unit.unitClass.framesToTurn180
    framesBeforeReacting >= framesBeforeContact
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val targetsNear = unit.matchups.targets.filter(t => unit.framesBeforeAttacking(t) < With.reaction.agencyMax)
    lazy val targetNear = new TargetAction(TargetFilterWhitelist(targetsNear))
    TargetInRange.delegate(unit)
    targetNear.delegate(unit)
    Target.delegate(unit)
    Attack.delegate(unit)
  }
}

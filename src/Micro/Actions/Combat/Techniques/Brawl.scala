package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.AttackMove
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Brawl extends ActionTechnique {
  
  // In close-quarters fights against other melee units,
  // prefer non-targeted commands to avoid glitching.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && ! unit.unitClass.isWorker
    && unit.matchups.targets.exists(_.unitClass.melee)
    && unit.matchups.threats.exists(_.unitClass.melee)
  )
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    lazy val brawlers = unit.matchups.threats.filter(t => ! t.flying && t.unitClass.melee && inBrawlRange(unit, t))
    if (unit.unitClass.melee && brawlers.size >= 3) 1.0 else 0.0
  }
  
  private def inBrawlRange(unit: FriendlyUnitInfo, other: UnitInfo): Boolean = {
    val framesBeforeContact = unit.pixelDistanceEdge(other) / (unit.topSpeed + other.topSpeed)
    val framesBeforeReacting = With.reaction.agencyMax + unit.unitClass.framesToTurn180
    framesBeforeReacting >= framesBeforeContact
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.unitClass.melee)
    Target.delegate(unit)
    unit.agent.toTravel = unit.agent.toAttack.map(_.pixelCenter)
    AttackMove.delegate(unit)
  }
}

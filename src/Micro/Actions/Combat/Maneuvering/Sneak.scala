package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import Planning.UnitMatchers.UnitMatchMobileDetectors
import Planning.Yolo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sneak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.cloaked
    && unit.canMove
    && ! Yolo.active
    && ! unit.matchups.allies.exists(_.is(Protoss.Arbiter))
    && ! unit.agent.canBerzerk
    && unit.matchups.enemies.exists(e => e.complete && ! e.unitClass.isWorker && (if (unit.flying) e.unitClass.attacksGround else e.unitClass.attacksAir))
    && ! unit.matchups.enemies.exists(_.is(UnitMatchMobileDetectors))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)

    if ( ! unit.readyForMicro) return

    val needsToFlee = (
      ! unit.effectivelyCloaked
        || unit.matchups.enemyDetectors.exists(e => e.pixelDistanceEdge(unit) < 32.0 * (if(e.unitClass.canMove) 15.0 else 12.0))
    )

    if (needsToFlee) {
      Target.delegate(unit)
      val forceThreat     = Potential.avoidThreatsWhileCloaked(unit)
      val forceSneaking   = Potential.detectionRepulsion(unit)
      unit.agent.forces.put(ForceColors.threat,     forceThreat)
      unit.agent.forces.put(ForceColors.bypassing,  forceSneaking)
      Gravitate.delegate(unit)
      Move.delegate(unit)
    }
  }
}

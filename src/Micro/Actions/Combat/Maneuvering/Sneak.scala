package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.Force
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
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
    && ! unit.matchups.threats.forall(_.unitClass.isWorker)
    && ( ! unit.effectivelyCloaked || unit.matchups.enemyDetectors.exists(e => e.pixelDistanceEdge(unit) < 32.0 * (if(e.unitClass.canMove) 15.0 else 12.0)))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    if (unit.readyForMicro) {
      Target.delegate(unit)
      val forceTarget     = unit.agent.toAttack.map(target => new Force(target.pixelCenter - unit.pixelCenter).normalize).getOrElse(new Force)
      val forceThreat     = Potential.avoidThreats(unit)
      val forceSneaking   = Potential.detectionRepulsion(unit)
      unit.agent.forces.put(ForceColors.threat,     forceThreat)
      unit.agent.forces.put(ForceColors.bypassing,  forceSneaking)
      Gravitate.delegate(unit)
      Move.delegate(unit)
    }
  }
}

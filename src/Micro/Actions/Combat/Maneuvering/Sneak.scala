package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sneak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.cloaked                                          &&
    ! unit.matchups.allies.exists(_.is(Protoss.Arbiter))  &&
    ! unit.agent.canBerzerk                               &&
    ! unit.matchups.threats.forall(_.unitClass.isWorker)  &&
    ( ! unit.effectivelyCloaked || unit.matchups.enemyDetectors.exists(e => e.pixelDistanceEdge(unit) < 32.0 * (if(e.unitClass.canMove) 15.0 else 12.0)))
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    if (unit.readyForMicro) {
      val forceThreat     = Potential.threatsRepulsion(unit)
      val forceSneaking   = Potential.detectionRepulsion(unit)
      val forceSpreading  = Potential.collisionRepulsion(unit)
      val forceMobility   = Potential.mobilityAttraction(unit)
      unit.agent.forces.put(ForceColors.threat,     forceThreat)
      unit.agent.forces.put(ForceColors.bypassing,  forceSneaking)
      unit.agent.forces.put(ForceColors.mobility,   forceMobility)
      unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
      Gravitate.delegate(unit)
    }
  }
}

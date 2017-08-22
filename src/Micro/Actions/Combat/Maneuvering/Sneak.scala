package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.Colors
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sneak extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.cloaked && ( ! unit.effectivelyCloaked ||
      unit.matchups.threats.nonEmpty &&
      unit.matchups.enemyDetectors.exists(e =>
        e.pixelDistanceFast(unit) < 32.0 * ( if(e.unitClass.canMove) 12.0 else 15.0))
    )
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    if (unit.readyForMicro) {
      val force = Potential.detectionRepulsion(unit)
      unit.agent.forces.put(Colors.NeonTeal, force)
      Gravitate.delegate(unit)
    }
  }
}

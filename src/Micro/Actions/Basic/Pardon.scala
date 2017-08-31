package Micro.Actions.Basic

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pardon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.agent.shovers.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forceSpreading  = Potential.collisionRepulsion(unit)
    val forceMobility   = Potential.mobilityAttraction(unit)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    Gravitate.delegate(unit)
  }
}

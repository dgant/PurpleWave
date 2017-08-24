package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.Colors
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Avoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.matchups.threats.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forceThreat     = Potential.threatsRepulsion(unit)
    val forceMobility   = Potential.barrierRepulsion(unit)
    val forceSpreading  = Potential.collisionRepulsion(unit)
    val forceExiting    = Potential.exitAttraction(unit)
    unit.agent.forces.put(Colors.NeonRed,     forceThreat)
    unit.agent.forces.put(Colors.NeonGreen,   forceMobility)
    unit.agent.forces.put(Colors.NeonViolet,  forceSpreading)
    unit.agent.forces.put(Colors.NeonBlue,    forceExiting)
    Gravitate.delegate(unit)
  }
}

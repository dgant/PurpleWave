package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
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
    val forceMobility   = Potential.mobilityAttraction(unit)
    val forceExiting    = Potential.exitAttraction(unit)
    val forceSpreading  = Potential.collisionRepulsion(unit)
    
    unit.agent.forces.put(ForceColors.threat,     forceThreat)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    unit.agent.forces.put(ForceColors.traveling,  forceExiting)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    Gravitate.delegate(unit)
  }
}

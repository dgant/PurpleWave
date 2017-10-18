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
    val threatBonus     = if (unit.matchups.threatsInRange.nonEmpty) 1.75 else 1.25
    val exitBonus       = if (unit.agent.origin.zone == unit.zone) 0.5 else 1.25
    val mobilityBonus   = 2.0 / Math.max(1.0, unit.mobility)
    val regroupingBonus = 18.0 / Math.max(24.0, unit.matchups.framesOfEntanglementCurrently)
    
    val forceThreat     = Potential.threatsRepulsion(unit).normalize(threatBonus)
    val forceExiting    = Potential.exitAttraction(unit).normalize(exitBonus)
    val forceMobility   = Potential.mobilityAttraction(unit).normalize(mobilityBonus)
    val forceSpreading  = Potential.collisionRepulsion(unit)
    val forceRegrouping = Potential.teamAttraction(unit).normalize(regroupingBonus)
    
    unit.agent.forces.put(ForceColors.threat,     forceThreat)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    unit.agent.forces.put(ForceColors.traveling,  forceExiting)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
    Gravitate.delegate(unit)
  }
}

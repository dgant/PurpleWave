package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object OldAvoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.matchups.threats.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val threatBonus     = PurpleMath.clamp(With.reaction.agencyAverage + unit.matchups.framesOfEntanglementCurrently, 12.0, 24.0) / 12.0
    val exitBonus       = if (unit.agent.origin.zone == unit.zone) 0.25 else 1.0
    val mobilityBonus   = 1.5 / Math.max(1.0, unit.mobility)
    val regroupingBonus = 12.0 / Math.max(24.0, unit.matchups.framesOfEntanglementCurrently)
    
    val forceThreat     = Potential.threatsRepulsion(unit)    * threatBonus
    val forceExiting    = Potential.exitAttraction(unit)      * exitBonus
    val forceMobility   = Potential.mobilityAttraction(unit)  * mobilityBonus
    val forceSpreading  = Potential.collisionRepulsion(unit)
    val forceRegrouping = Potential.teamAttraction(unit)      * regroupingBonus
    //val forceOptimizing = (forceExiting + forceSpreading + forceMobility).clipMin(1.0)
    
    unit.agent.forces.put(ForceColors.threat,     forceThreat)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    unit.agent.forces.put(ForceColors.traveling,  forceExiting)
    //unit.agent.forces.put(ForceColors.traveling,  forceOptimizing)
    unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
    unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}

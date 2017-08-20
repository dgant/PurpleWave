package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.Colors
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.PotentialFieldMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Avoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.matchups.threats.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forceThreat     = PotentialFieldMath.threatForce(unit)
    val forceMobility   = PotentialFieldMath.mobilityForce(unit)
    val forceSpreading  = PotentialFieldMath.spreadingForce(unit)
    unit.agent.forces.put(Colors.NeonRed,     forceThreat)
    unit.agent.forces.put(Colors.NeonGreen,   forceMobility)
    unit.agent.forces.put(Colors.NeonViolet,  forceSpreading)
    Gravitate.delegate(unit)
  }
}

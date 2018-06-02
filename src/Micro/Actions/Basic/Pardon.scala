package Micro.Actions.Basic

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pardon extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.agent.shovers.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val forcesPardoning = unit.agent.shovers.map(shover => Potential.unitAttraction(unit, shover, -1.0))
    val forcePardoning  = forcesPardoning.reduce(_ + _).normalize
    val forcesMobility  = Potential.resistTerrain(unit)
    unit.agent.forces.put(ForceColors.spacing, forcePardoning)
    unit.agent.resistances.put(ForceColors.mobility, forcesMobility)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}

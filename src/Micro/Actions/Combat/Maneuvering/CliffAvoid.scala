package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Heuristics.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CliffAvoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.flying
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val threats           = unit.matchups.threats
    val walkers           = threats.filter(threat => ! threat.flying && threat.zone == unit.zone)
    val dpfFromWalkers    = walkers.map(_.dpfOnNextHitAgainst(unit)).sum
    val dpfFromThreats    = threats.map(_.dpfOnNextHitAgainst(unit)).sum
    val cliffingMagnitude = PurpleMath.nanToZero(dpfFromWalkers / dpfFromThreats)

    val forceThreats = Potential.avoidThreats(unit)
    val forceCliffing = Potential.cliffAttraction(unit).normalize(0.5 * cliffingMagnitude)
    unit.agent.forces.put(ForceColors.sneaking, forceCliffing)
    
    Retreat.delegate(unit)
  }
}

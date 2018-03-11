package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CliffAvoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove  &&
    unit.flying   &&
    unit.matchups.threats.exists( ! _.flying)
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val threats           = unit.matchups.threats
    val walkers           = threats.filter(threat => ! threat.flying && threat.zone == unit.zone)
    val damageFromWalkers = walkers.map(_.damageOnNextHitAgainst(unit)).sum
    val damageFromThreats = threats.map(_.damageOnNextHitAgainst(unit)).sum
    val cliffingMagnitude = PurpleMath.nanToZero(damageFromWalkers / damageFromThreats)
    
    if (cliffingMagnitude <= 0) return
    
    val forceThreat     = Potential.threatsRepulsion(unit)
    val forceMobility   = Potential.mobilityAttraction(unit)
    val forceCliffing   = Potential.cliffAttraction(unit).normalize(cliffingMagnitude)
    val forceRegrouping = Potential.teamAttraction(unit)
    
    unit.agent.forces.put(ForceColors.threat,     forceThreat)
    unit.agent.forces.put(ForceColors.mobility,   forceMobility)
    unit.agent.forces.put(ForceColors.bypassing,  forceCliffing)
    unit.agent.forces.put(ForceColors.regrouping, forceRegrouping)
    Gravitate.delegate(unit)
    Move.delegate(unit)
  }
}

package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object BeAnArbiter extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete   &&
    unit.is(Protoss.Arbiter)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val umbrellable = (u: UnitInfo) => ! u.unitClass.isBuilding && u != unit
    var friends: Seq[UnitInfo] = unit.squadmates.filter(umbrellable)
    if ( ! friends.exists(_.pixelDistanceFast(unit) < 32.0 * 20.0)) {
      friends = unit.matchups.allies.filter(umbrellable)
    }
    else {
      val threats = unit.matchups.threats
      val threatMagnitude = if (unit.matchups.threatsInRange.nonEmpty) 1.0 else 0.25
      val forcesUmbrella  = friends.map(friend => Potential.unitAttraction(unit, friend, 1.0 / Math.max(24.0, friend.matchups.framesOfSafetyDiffused)))
      val forcesThreats   = threats.map(threat => Potential.unitAttraction(unit, threat, 1.0 / Math.max(24.0, threat.framesBeforeAttacking(unit))))
      val forceUmbrella   = ForceMath.sum(forcesUmbrella).normalize
      val forceThreats    = ForceMath.sum(forcesThreats).normalize(threatMagnitude)
      unit.agent.forces.put(ForceColors.regrouping, forceUmbrella)
      unit.agent.forces.put(ForceColors.threat,     forceThreats)
      Gravitate.consider(unit)
    }
  }
}

package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Avoid
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
    val umbrellable = (u: UnitInfo) => ! u.unitClass.isBuilding
    var friends: Seq[UnitInfo] = unit.squadmates.filter(umbrellable)
    if (friends.isEmpty) {
      friends = unit.matchups.allies.filter(umbrellable)
    }
    if (friends.isEmpty) {
      Avoid.consider(unit)
    }
    if (unit.matchups.framesOfSafetyDiffused < 24) {
      Avoid.consider(unit)
    }
    else {
      val threats = unit.matchups.threats
      val forcesUmbrella  = friends.map(friend => Potential.unitAttraction(unit, friend, 1.0 / Math.max(24.0, friend.matchups.framesOfSafetyDiffused)))
      val forcesThreats   = threats.map(threat => Potential.unitAttraction(unit, threat, 1.0 / Math.max(24.0, threat.framesBeforeAttacking(unit))))
      val forceUmbrella   = ForceMath.sum(forcesUmbrella)
      unit.agent.forces.put(ForceColors.spreading,  forceUmbrella)
      Gravitate.consider(unit)
    }
  }
}

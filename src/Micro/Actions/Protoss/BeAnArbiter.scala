package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Avoid
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeAnArbiter extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete           &&
    unit.is(Protoss.Arbiter)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val friends = unit.squadmates.filter(friend => ! friend.unitClass.isBuilding)
    if (friends.isEmpty) {
      Avoid.consider(unit)
    }
    else {
      val threats = unit.matchups.threats
      val forcesUmbrella  = friends.map(friend => Potential.unitAttraction(unit, friend, 1.0 / Math.max(24.0, friend.matchups.framesOfSafetyDiffused)))
      val forcesThreats   = threats.map(threat => Potential.unitAttraction(unit, threat, 1.0 / Math.max(24.0, threat.framesBeforeAttacking(unit))))
      val forceUmbrella   = Potential.sum(forcesUmbrella)
      val forceThreat     = Potential.sum(forcesThreats)
      Gravitate.consider(unit)
    }
  }
}

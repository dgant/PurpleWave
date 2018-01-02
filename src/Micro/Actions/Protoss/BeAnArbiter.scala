package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.OldAvoid
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import Planning.Yolo
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object BeAnArbiter extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.aliveAndComplete     &&
    unit.is(Protoss.Arbiter)  &&
    unit.matchups.enemies.exists(_.matchups.targets.nonEmpty)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.consider(unit)
    
    lazy val threatened   = unit.matchups.framesOfSafetyDiffused <= 0.0
    lazy val umbrellable  = (u: UnitInfo) => ! u.unitClass.isBuilding && u != unit && ! u.is(Protoss.Interceptor) && ! u.is(Protoss.Arbiter)
    lazy val friends      = unit.teammates.filter(umbrellable)
    
    if (threatened && ! Yolo.active) {
      OldAvoid.consider(unit)
    }
    else if (friends.nonEmpty) {
      val threats = unit.matchups.threats
      val threatMagnitude = if (unit.matchups.threatsInRange.nonEmpty) 1.0 else 0.25
      val forcesUmbrella  = friends.map(friend => Potential.unitAttraction(unit, friend, 1.0 / Math.max(24.0, Math.min(240.0, friend.matchups.framesOfSafetyDiffused))))
      val forcesThreats   = threats.map(threat => Potential.unitAttraction(unit, threat, 1.0 / Math.max(24.0, Math.min(240.0, threat.framesBeforeAttacking(unit)))))
      val forceUmbrella   = ForceMath.sum(forcesUmbrella).normalize
      val forceThreats    = ForceMath.sum(forcesThreats).normalize(threatMagnitude)
      unit.agent.forces.put(ForceColors.regrouping, forceUmbrella)
      unit.agent.forces.put(ForceColors.threat,     forceThreats)
      Gravitate.consider(unit)
    }
  }
}

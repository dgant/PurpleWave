package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
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
    
    val presumedDelay = 48.0
    
    lazy val threatened   = unit.matchups.framesOfSafetyDiffused <= 0.0
    lazy val umbrellable  = (u: UnitInfo) => ! u.unitClass.isBuilding && u != unit && ! u.is(Protoss.Interceptor) && ! u.is(Protoss.Arbiter)
    lazy val friends      = unit.teammates.filter(umbrellable)

    if (friends.nonEmpty) {
      val forcesUmbrella = friends.map(friend =>
        Potential.unitAttraction(
          unit,
          friend,
          Math.max(1.0, 48.0 + friend.matchups.framesOfEntanglementDiffused)))
      
      val forcesThreats = unit.matchups.enemies
        .map(enemy =>
          Potential.unitAttraction(
            unit,
            enemy,
            enemy.matchups.targets.size
            + 2.0 * enemy.matchups.targetsInRange.size))
  
      val forceUmbrella = ForceMath.sum(forcesUmbrella).normalize
      val forceThreats  = ForceMath.sum(forcesThreats).normalize(0.5)
      val forceForward  = ForceMath.fromPixels(unit.pixelCenter, unit.agent.destination, 0.5)
      unit.agent.forces.put(ForceColors.regrouping, forceUmbrella)
      unit.agent.forces.put(ForceColors.threat,     forceThreats)
      unit.agent.forces.put(ForceColors.target,     forceForward)
      Gravitate.consider(unit)
    }
    if (threatened) {
      Avoid.consider(unit)
    }
    Move.delegate(unit)
  }
}

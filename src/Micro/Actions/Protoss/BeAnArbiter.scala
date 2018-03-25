package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel._

object BeAnArbiter extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.aliveAndComplete
    && unit.is(Protoss.Arbiter)
    && unit.matchups.enemies.exists(_.matchups.targets.nonEmpty)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.consider(unit)
    
    val threatened      = unit.matchups.framesOfSafetyDiffused <= 0.0
    val umbrellable     = (u: UnitInfo) => ! u.unitClass.isBuilding && u != unit && ! u.is(Protoss.Interceptor) && ! u.is(Protoss.Arbiter)
    val friends         = unit.teammates.filter(umbrellable)
    
    if (friends.nonEmpty) {
      val friendCentroid = friends.map(_.pixelCenter).centroid
      unit.agent.toTravel = Some(friendCentroid)
    }
    
    val framesOfSafetyRequired = Math.max(0, 48 - With.framesSince(unit.lastFrameTakingDamage))
    if (unit.matchups.framesOfSafetyDiffused <= framesOfSafetyRequired) {
      Avoid.delegate(unit)
    }
    else if (friends.nonEmpty) {
      val forcesThreats = unit.matchups.enemies
        .map(enemy =>
          Potential.unitAttraction(
            unit,
            enemy,
            enemy.matchups.targets.size
            + 2.0 * enemy.matchups.targetsInRange.size))
  
      val forceUmbrella = new Force(unit.agent.destination.subtract(unit.pixelCenter)).normalize
      val forceThreats  = ForceMath.sum(forcesThreats)
      unit.agent.forces.put(ForceColors.regrouping, forceUmbrella)
      unit.agent.forces.put(ForceColors.threat,     forceThreats)
      Gravitate.consider(unit)
    }
    
    Move.delegate(unit)
  }
}

package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Gravitate, Move}
import Micro.Decisions.Potential
import Micro.Heuristics.Spells.TargetAOE
import Planning.Composition.UnitMatchers.UnitMatchBuilding
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object BeAnArbiter extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.aliveAndComplete && unit.is(Protoss.Arbiter)
  )
  
  protected def needsUmbrella(target: UnitInfo): Boolean =
    ! target.isAny(
      Protoss.Arbiter,
      Protoss.DarkTemplar,
      Protoss.Interceptor,
      Protoss.Observer,
      UnitMatchBuilding)
  
  protected def evaluateForCloaking(target: UnitInfo): Double = {
    if ( ! target.isFriendly) return 0.0
    if ( ! needsUmbrella(target)) return 0.0
    val value       = target.subjectiveValue
    val multiplier  = 2.0 + PurpleMath.fastSigmoid(Math.max(-48, target.matchups.framesOfEntanglementDiffused))
    val output      = value * multiplier
    output
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.consider(unit)
    
    val threatened   = unit.matchups.framesOfSafetyDiffused <= 12.0
    val needUmbrella = unit.teammates.filter(needsUmbrella)
    
    if (needUmbrella.nonEmpty) {
      val destination = TargetAOE.chooseTargetPixel(
        unit,
        32 * 15,
        Double.NegativeInfinity,
        evaluateForCloaking,
        24,
        (tile) => Circle.points(3).map(tile.add))
      unit.agent.toTravel = destination.orElse(
        Some(needUmbrella
          .minBy(_.pixelDistanceCenter(unit.agent.destination))
          .pixelCenter))
    }
    
    val framesOfSafetyRequired = Math.max(0, 48 - With.framesSince(unit.lastFrameTakingDamage))
    if (unit.matchups.framesOfSafetyDiffused <= framesOfSafetyRequired) {
      Avoid.delegate(unit)
    }
    else if (needUmbrella.nonEmpty) {
      val forcesThreats = unit.matchups.threats
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

package Micro.Actions.Protoss

import Debugging.Visualizations.ForceColors
import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
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
    val multiplier  = 2.0 + PurpleMath.fastTanh(Math.max(-48, target.matchups.framesOfEntanglement))
    val output      = value * multiplier
    output
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.consider(unit)
    
    val umbrellaSearchRadius  = 32.0 * 20.0
    val threatened            = unit.matchups.framesOfSafety <= 12.0
    val needUmbrella          = unit.teammates.filter(needsUmbrella)
    val needUmbrellaNearby    = needUmbrella.filter(_.pixelDistanceCenter(unit) < umbrellaSearchRadius)
    
    if (needUmbrella.isEmpty) {
      unit.agent.shouldEngage = false
    }
    else {
      val destination = TargetAOE.chooseTargetPixel(
        unit,
        umbrellaSearchRadius,
        Protoss.Zealot.subjectiveValue,
        evaluateForCloaking,
        24,
        (tile) => Circle.points(3).map(tile.add),
        Some(needUmbrellaNearby))
      unit.agent.toTravel = destination.orElse(
        Some(needUmbrella
          .minBy(_.pixelDistanceCenter(unit.agent.destination))
          .pixelCenter))
    }
  
    val forceUmbrella = new Force(unit.agent.destination.subtract(unit.pixelCenter)).normalize
    val framesOfSafetyRequired = Math.max(12, 48 - With.framesSince(unit.lastFrameTakingDamage))
    if (unit.matchups.framesOfSafety <= framesOfSafetyRequired) {
      val forceThreat = Potential.threatsRepulsion(unit).normalize(3.0)
      unit.agent.forces.put(ForceColors.regrouping, forceUmbrella)
      unit.agent.forces.put(ForceColors.threat, forceThreat)
      Gravitate.consider(unit)
    }
    else if (needUmbrella.nonEmpty) {
      val forcesThreats = unit.matchups.threats
        .map(enemy =>
          Potential.unitAttraction(
            unit,
            enemy,
                    enemy.matchups.targets.size
            + 2.0 * enemy.matchups.targetsInRange.size))
  
      val forceThreats  = ForceMath.sum(forcesThreats)
      unit.agent.forces.put(ForceColors.regrouping, forceUmbrella)
      unit.agent.forces.put(ForceColors.target,     forceThreats)
      Gravitate.consider(unit)
    }
    
    Move.delegate(unit)
  }
}

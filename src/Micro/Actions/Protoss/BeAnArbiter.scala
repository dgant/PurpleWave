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
import Planning.UnitMatchers.UnitMatchBuilding
import ProxyBwapi.Races.{Protoss, Terran}
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

  override protected def perform(arbiter: FriendlyUnitInfo) {
    val umbrellaSearchRadius    = 32.0 * 20.0
    val threatened              = arbiter.matchups.framesOfSafety <= 12.0
    val arbiters                = arbiter.teammates.filter(_.is(Protoss.Arbiter))
    val needUmbrella            = arbiter.teammates.toSeq.filter(needsUmbrella)
    val needUmbrellaNearby      = needUmbrella.filter(_.pixelDistanceCenter(arbiter) < umbrellaSearchRadius)
    lazy val needUmbrellaBadly  = needUmbrellaNearby.filter(_.friendly.forall(_.agent.umbrellas.isEmpty))
    val toUmbrella              = if (arbiter.matchups.enemies.exists(_.is(Terran.ScienceVessel))) needUmbrellaBadly else needUmbrellaNearby

    def evaluateForCloaking(target: UnitInfo): Double = {
      if ( ! target.isFriendly) return 0.0
      if ( ! needsUmbrella(target)) return 0.0
      if (target.battle.isEmpty && arbiter.squad.exists(ourSquad => ! target.friendly.map(_.squad).exists(_.contains(ourSquad)))) return 0.0
      val value           = target.subjectiveValue
      val dangerFactor    = 2.0 + PurpleMath.fastTanh(Math.max(-48, target.matchups.framesOfEntanglement))
      val isolationFactor = if (target.friendly.exists(_.agent.umbrellas.nonEmpty) && target.matchups.nearestArbiter.exists(_ != arbiter)) 0.0 else 1.0
      val output          = value * dangerFactor * isolationFactor
      output
    }

    if (needUmbrella.nonEmpty) {
      val destination = TargetAOE.chooseTargetPixel(
        arbiter,
        umbrellaSearchRadius,
        Protoss.Zealot.subjectiveValue,
        evaluateForCloaking,
        12,
        (tile) => Circle.points(2).map(tile.add).filter(_.valid),
        Some(needUmbrellaNearby))
      destination.foreach(someDestination =>
        needUmbrella.foreach(ally => if (ally.pixelDistanceCenter(someDestination) < 32 * 7) {
          ally.friendly.foreach(_.agent.addUmbrella(arbiter))
        }))
      arbiter.agent.toTravel = destination.orElse(
        Some(needUmbrella
          .minBy(_.pixelDistanceCenter(arbiter.agent.destination))
          .pixelCenter))
    }

    val forceUmbrella = new Force(arbiter.agent.destination.subtract(arbiter.pixelCenter)).normalize
    val framesOfSafetyRequired = Math.max(0, 48 - With.framesSince(arbiter.lastFrameTakingDamage))
    if (arbiter.matchups.framesOfSafety <= framesOfSafetyRequired) {
      val forceThreat = Potential.avoidThreats(arbiter)
      val forceEMP = Potential.avoidEmp(arbiter)
      val resistancesTerrain = Potential.resistTerrain(arbiter)
      arbiter.agent.forces.put(ForceColors.regrouping,  forceUmbrella)
      arbiter.agent.forces.put(ForceColors.spacing,     forceEMP)
      arbiter.agent.forces.put(ForceColors.threat,      forceThreat)
      arbiter.agent.resistances.put(ForceColors.mobility, resistancesTerrain)
      Gravitate.consider(arbiter)
      Move.delegate(arbiter)
      arbiter.agent.fightReason = "Unsafe"
    } else if (needUmbrella.nonEmpty) {
      Potshot.delegate(arbiter)
      val forcesThreats = arbiter.matchups.enemies
        .map(enemy =>
          Potential.unitAttraction(
            arbiter,
            enemy,
                    enemy.matchups.targets.size
            + 2.0 * enemy.matchups.targetsInRange.size))
      val forceThreats = ForceMath.sum(forcesThreats).normalize
      val forceEMP = Potential.avoidEmp(arbiter)
      arbiter.agent.forces.put(ForceColors.threat,      forceThreats)
      arbiter.agent.forces.put(ForceColors.spacing,     forceEMP)
      arbiter.agent.forces.put(ForceColors.regrouping,  forceUmbrella)
      Gravitate.consider(arbiter)
      Move.delegate(arbiter)
      arbiter.agent.fightReason = "Umbrella"
    }
    if (arbiter.matchups.framesOfSafety < 48 || arbiter.matchups.threats.exists(_.topSpeed > arbiter.topSpeed)) {
      arbiter.agent.shouldEngage = false
    }
  }
}

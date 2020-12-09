package Micro.Actions.Protoss

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Heuristics.SpellTargetAOE
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

  def evaluateForCloaking(target: UnitInfo, friendlyUnitInfo: FriendlyUnitInfo): Double = {
    if ( ! target.isFriendly) return 0.0
    if ( ! needsUmbrella(target)) return 0.0
    if (target.battle.isEmpty && friendlyUnitInfo.squad.exists(ourSquad => ! target.friendly.map(_.squad).exists(_.contains(ourSquad)))) return 0.0
    val value           = target.subjectiveValue
    val dangerFactor    = 2.0 + PurpleMath.fastTanh(Math.max(-64, target.matchups.pixelsOfEntanglement))
    val isolationFactor = if (target.friendly.exists(_.agent.umbrellas.nonEmpty) && target.matchups.nearestArbiter.exists(_ != friendlyUnitInfo)) 0.0 else 1.0
    val output          = value * dangerFactor * isolationFactor
    output
  }

  override protected def perform(arbiter: FriendlyUnitInfo) {
    val umbrellaSearchRadius    = 32.0 * 20.0
    val threatened              = arbiter.matchups.framesOfSafety <= 12.0 && ! With.yolo.active()
    val arbiters                = arbiter.teammates.filter(_.is(Protoss.Arbiter))
    val needUmbrella            = arbiter.teammates.toSeq.filter(needsUmbrella)
    val needUmbrellaNearby      = needUmbrella.filter(_.pixelDistanceCenter(arbiter) < umbrellaSearchRadius)
    val needUmbrellaBadly       = needUmbrellaNearby.filter(_.friendly.forall(_.agent.umbrellas.isEmpty))
    val toUmbrella              = if (arbiter.matchups.enemies.exists(_.is(Terran.ScienceVessel))) needUmbrellaBadly else needUmbrellaNearby
    var amCovering              = false

    if (needUmbrella.nonEmpty && arbiter.battle.isDefined) {
      val destination = new SpellTargetAOE().chooseTargetPixel(
        arbiter,
        umbrellaSearchRadius,
        Protoss.Zealot.subjectiveValue,
        evaluateForCloaking,
        pixelWidth = 288,
        pixelHeight = 288,
        projectionFrames = 12.0,
        candidates = Some(toUmbrella))
      destination.foreach(someDestination => {
        amCovering = true
        needUmbrella.foreach(ally => if (ally.pixelDistanceCenter(someDestination) < 32 * 7) {
          ally.friendly.foreach(_.agent.addUmbrella(arbiter))
        })})

      arbiter.agent.toReturn = destination.orElse(
        Some(needUmbrella
          .minBy(_.pixelDistanceCenter(arbiter.agent.destination))
          .pixelCenter))
      arbiter.agent.toTravel = arbiter.agent.toReturn
    }

    val forceUmbrella = new Force(arbiter.agent.destination.subtract(arbiter.pixelCenter)).normalize
    val framesOfSafetyRequired = Math.max(0, 48 - With.framesSince(arbiter.lastFrameTakingDamage))
    if (arbiter.matchups.framesOfSafety <= framesOfSafetyRequired
      || (arbiter.energy > 40 && arbiter.matchups.enemyDetectors.exists(e => e.is(Terran.ScienceVessel) && e.pixelDistanceEdge(arbiter) < 32 * 6))) {
      Retreat.consider(arbiter)
      arbiter.agent.fightReason = "Unsafe"
    } else if (amCovering) {
      Potshot.delegate(arbiter)
      Retreat.consider(arbiter)
      arbiter.agent.fightReason = "Umbrella"
    }
    if (arbiter.matchups.framesOfSafety < 48 || arbiter.matchups.threats.exists(_.topSpeed > arbiter.topSpeed)) {
      arbiter.agent.shouldEngage = false
    }
  }
}

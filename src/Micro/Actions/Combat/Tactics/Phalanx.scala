package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.util.Random

object Phalanx extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    ! unit.flying
    && unit.agent.toForm.exists(p => unit.framesToTravelTo(p) < GameTime(0, 10)()))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val spot      = unit.agent.toForm.get
    val hoplites  = unit.matchups.allies.flatMap(_.friendly).filter(_.agent.toForm.isDefined)
    val openFire  = unit.matchups.targets.exists(t => t.pixelDistanceEdge(unit, unit.agent.toForm.get) <= unit.pixelRangeAgainst(t))
    val besieged = hoplites.exists(hoplite =>
      hoplite.matchups.threats.exists(threat =>
        threat.inRangeToAttack(hoplite, hoplite.agent.toForm.get)
        && hoplite.pixelRangeAgainst(threat) < threat.pixelRangeAgainst(hoplite)
        && With.grids.enemyVision.isSet(hoplite.agent.toForm.get.tileIncluding)))

    unit.agent.toTravel = Some(spot)
    unit.agent.toReturn = Some(spot)
    val formationDistance = unit.pixelDistanceCenter(unit.agent.toForm.get)
    if (formationDistance <= 10
      && unit.unitClass.melee
      && (
        unit.matchups.targetsInRange.nonEmpty
        || unit.matchups.threatsInRange.isEmpty
        || ! unit.visibleToOpponents)) {
      if (unit.matchups.framesOfSafety < 24 || (formationDistance < 4 && ! unit.moving)) {
        With.commander.hold(unit)
      } else {
        if (Random.nextInt(10) == 0) {
          def spread(value: Int) = value + 5 - Random.nextInt(10)
          unit.agent.toTravel = unit.agent.toTravel.map(p => Pixel(spread(p.x), spread(p.y)))
        }
        Move.delegate(unit)
      }
    } else if (besieged && unit.agent.shouldEngage) {
      Engage.delegate(unit)
    } else if (openFire) {
      Engage.delegate(unit)
    } else if (unit.matchups.threats.exists(! _.unitClass.melee))
    if (unit.matchups.threats.exists(! _.unitClass.isWorker)) {
      Disengage.delegate(unit)
    }
    Move.delegate(unit)
  }
}

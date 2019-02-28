package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Commands.Move
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.util.Random

object Phalanx extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    ! unit.flying
    && With.geography.ourBases.size < 3
    && unit.agent.toForm.exists(p => unit.framesToTravelTo(p) < GameTime(0, 8)()))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val spot      = unit.agent.toForm.get
    val hoplites  = unit.matchups.allies.flatMap(_.friendly).filter(_.agent.toForm.isDefined)
    val openFire  = ! unit.unitClass.melee && unit.matchups.targets.exists(t => t.pixelDistanceEdge(unit, unit.agent.toForm.get) <= unit.pixelRangeAgainst(t))
    val besieged = hoplites.exists(hoplite =>
      hoplite.matchups.threats.exists(threat =>
        threat.inRangeToAttack(hoplite, hoplite.agent.toForm.get)
        && hoplite.pixelRangeAgainst(threat) < threat.pixelRangeAgainst(hoplite)
        && With.grids.enemyVision.isSet(hoplite.agent.toForm.get.tileIncluding)))

    lazy val soloZealotShouldFlee = (
      unit.is(Protoss.Zealot)
      && unit.matchups.allies.count(_.is(UnitMatchWarriors)) == 0
      && unit.matchups.threats.count(_.is(Protoss.Zealot)) > 1)

    unit.agent.toTravel = Some(spot)
    unit.agent.toReturn = Some(spot)
    val formationDistance = unit.pixelDistanceCenter(unit.agent.toForm.get)
    if (formationDistance <= 12
      && unit.unitClass.melee
      && (
        unit.matchups.targetsInRange.nonEmpty
        || unit.matchups.threatsInRange.isEmpty
        || ! unit.visibleToOpponents)
      && ! soloZealotShouldFlee) {
      if (unit.matchups.targetsInRange.nonEmpty) {
        Potshot.consider(unit)
      } else if (unit.matchups.framesOfSafety < 24 || (formationDistance < 4 && ! unit.moving)) {
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

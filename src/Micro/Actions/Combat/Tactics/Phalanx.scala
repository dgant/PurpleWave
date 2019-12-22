package Micro.Actions.Combat.Tactics

import Information.Geography.Pathfinding.PathfindProfile
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Combat.Maneuvering.Traverse
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

    val acceptableDistanceFromFormation = 12

    val spot      = unit.agent.toForm.get
    val hoplites  = unit.matchups.alliesInclSelf.flatMap(_.friendly).filter(_.agent.toForm.isDefined)
    val openFire  = ! unit.unitClass.melee && unit.matchups.targets.exists(t =>  t.pixelDistanceEdge(unit, unit.agent.toForm.get) <= unit.pixelRangeAgainst(t))
    val besieged  = hoplites.exists(hoplite =>
      hoplite.agent.toForm.isDefined
      && hoplite.matchups.threats.exists(threat =>
        (
          threat.inRangeToAttack(hoplite, hoplite.agent.toForm.get)
          || (threat.inRangeToAttack(hoplite) && hoplite.pixelDistanceCenter(hoplite.agent.toForm.get) <= acceptableDistanceFromFormation))
        && hoplite.pixelRangeAgainst(threat) <= threat.pixelRangeAgainst(hoplite)
        && With.grids.enemyVision.isSet(hoplite.agent.toForm.get.tileIncluding)))

    lazy val soloZealotShouldFlee = (
      unit.is(Protoss.Zealot)
      && unit.matchups.allies.count(_.is(UnitMatchWarriors)) == 0
      && unit.matchups.threats.count(_.is(Protoss.Zealot)) > 1)

    unit.agent.toTravel = Some(spot)
    unit.agent.toReturn = Some(spot)
    val formationDistance = unit.pixelDistanceCenter(unit.agent.toForm.get)
    if (formationDistance <= acceptableDistanceFromFormation
      && unit.unitClass.melee
      && (
        unit.matchups.targetsInRange.nonEmpty
        || unit.matchups.threatsInRange.isEmpty
        || ! unit.visibleToOpponents)
      && ! soloZealotShouldFlee) {
      if (unit.matchups.targetsInRange.nonEmpty) {
        Potshot.consider(unit)
      } else if (unit.matchups.framesOfSafety < 24 || (formationDistance < 4 && ! unit.moving)) {
        // Addressing the case where BW's in-range calculation differs from ours -- this avoids holding position where a unit refuses to fight back
        if (unit.matchups.framesOfSafety < 2) {
          Engage.delegate(unit)
        }
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
      // If we can safely get to our spot, let's do so
      val profile = new PathfindProfile(unit.tileIncludingCenter)
      profile.end                 = unit.agent.toForm.map(_.tileIncluding)
      profile.lengthMaximum       = Some(2 + formationDistance.toInt * 2 / 32)
      profile.canCrossUnwalkable  = unit.transport.exists(_.flying)
      profile.allowGroundDist     = false
      profile.costThreat          = 3f
      profile.unit = Some(unit)
      val path = profile.find
      new Traverse(path).delegate(unit)
      Disengage.delegate(unit)
    }
    Move.delegate(unit)
  }
}

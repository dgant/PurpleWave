package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ExplosionHop extends Action {

  def explodeysTargeting(unit: FriendlyUnitInfo): Boolean =
    unit.matchups.enemies.exists(explodey =>
      explodey.isAny(Terran.SpiderMine, Protoss.Scarab)
      && explodey.presumptiveTarget.contains(unit))

  def getNearestTransport(unit: FriendlyUnitInfo): Option[FriendlyUnitInfo] = {
    ByOption.minBy(unit.matchups.allies.view
      .flatMap(_.friendly)
      .filter(_.canTransport(unit)))(_.pixelDistanceEdge(unit))
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && getNearestTransport(unit).isDefined
    && explodeysTargeting(unit))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val transport = unit.agent.ride.getOrElse(getNearestTransport(unit).get)
    if (transport.framesToTravelPixels(transport.pixelDistanceEdge(unit)) < 24) {
      transport.agent.claimPassenger(unit)
      unit.agent.hoppingExplosion = true
      With.commander.rightClick(unit, transport)
    }
  }
}

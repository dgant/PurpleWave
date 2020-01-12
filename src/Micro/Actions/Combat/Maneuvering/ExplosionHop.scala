package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ExplosionHop extends Action {

  def explodeysTargeting(unit: FriendlyUnitInfo): Boolean = (
    ! With.enemies.forall(_.isZerg)
    && unit.matchups.enemies.exists(explodey =>
      explodey.isAny(Terran.SpiderMine, Protoss.Scarab)
      && explodey.presumptiveTarget.contains(unit)))

  def getNearestTransport(unit: FriendlyUnitInfo): Option[FriendlyUnitInfo] = {
    ByOption.minBy(unit.inTileRadius(2)
      .filter(_.friendly.exists(_.canTransport(unit)))
      .flatMap(_.friendly))(_.pixelDistanceEdge(unit))
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && explodeysTargeting(unit)
    && (unit.agent.ride.isDefined || getNearestTransport(unit).isDefined))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val transport = unit.agent.ride.getOrElse(getNearestTransport(unit).get)
    if (transport.framesToTravelPixels(transport.pixelDistanceEdge(unit)) < 24) {
      transport.agent.claimPassenger(unit)
      unit.agent.hoppingExplosion = true
      With.commander.rightClick(unit, transport)
    }
  }
}

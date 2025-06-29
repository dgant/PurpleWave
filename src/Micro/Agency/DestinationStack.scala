package Micro.Agency

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Micro.Agency.Destinations.DestinationLevel
import Performance.Cache
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

trait DestinationStack {
  val unit: FriendlyUnitInfo

  val destinations: Vector[Destination] = Destinations.All.map(new Destination(unit, _))
  var path: Option[TilePath] = None

  private def at  (destinationLevel: DestinationLevel)                    : Destination = destinations(destinationLevel.level - destinations.head.level)
  private def set (destinationLevel: DestinationLevel, p: Pixel)          : Destination = at  (destinationLevel).set(p)
  private def set (destinationLevel: DestinationLevel, t: Tile)           : Destination = set (destinationLevel, t.center)
  private def set (destinationLevel: DestinationLevel, p: Option[Pixel])  : Destination = at  (destinationLevel).set(p)
  private def sett(destinationLevel: DestinationLevel, t: Option[Tile])   : Destination = set (destinationLevel, t.map(_.center))

  val home      : Destination = at(Destinations.Home).set(unit.pixel)
  val redoubt   : Destination = at(Destinations.Redoubt)
  val terminus  : Destination = at(Destinations.Terminus).set(unit.pixel)
  val perch     : Destination = at(Destinations.Perch)
  val station   : Destination = at(Destinations.Station)
  val forced    : Destination = at(Destinations.Forced)
  val decision  : Destination = at(Destinations.Decision)

  def destinationsDefined : Seq[Destination]  = destinations.view.filter(_.pixel.isDefined)
  def origin              : Destination       = destinationsDefined.reverseIterator.find(_.level < 0).getOrElse(home)
  def destinationFinal    : Destination       = Maff.minBy(destinationsDefined)(d => ?(d.level < 0, 100, 0) + d.level).getOrElse(terminus)
  def destinationNext     : Destination       = Maff.minBy(destinationsDefined)(d => ?(d.level < 0, 100, 0) - d.level).getOrElse(terminus)

  def resetDestinations(): Unit = {
    destinations.foreach(_.clear())
    path = None
    home.set(defaultHome)
    terminus.set(unit.pixel)
    unit.intent.redoubt                                 .foreach(redoubt.set)
    unit.stationDisengage                               .foreach(redoubt.set) // Formation supersedes intent; intent is used to provide a default value
    unit.intent.toNuke                                  .foreach(terminus.set)
    unit.intent.toAttack      .map(_.pixel)             .foreach(terminus.set)
    unit.intent.toGather      .map(_.pixel)             .foreach(terminus.set)
    unit.intent.toHeal      .map(_.pixel)             .foreach(terminus.set)
    unit.intent.toFinish      .map(_.pixel)             .foreach(terminus.set)
    unit.intent.toBoard       .map(_.pixel)             .foreach(terminus.set)
    unit.intent.toBuildActive .map(_.tile.topLeftPixel) .foreach(terminus.set)
    unit.intent.toScoutTiles.headOption                 .foreach(terminus.set)
    unit.intent.terminus                                .foreach(terminus.set) // Explicit intent supersedes implicit intent
    unit.intent.station                                 .foreach(station.set)
    unit.stationEngage                                  .foreach(station.set) // Formation supersedes intent; intent is used to provide a default value
  }

  def safety: Pixel = unit.agent
    .ride.filterNot(unit.transport.contains).map(_.pixel)
    .getOrElse(origin())

  def defaultHome: Pixel = _defaultHome()
  private val _defaultHome = new Cache[Pixel](() =>
    Maff.minBy(
      With.geography.ourBases.filter(base =>
        base.scoutedByEnemy
          || base.naturalOf.exists(_.scoutedByEnemy)
          || base.metro == With.geography.ourMetro))(base =>
      unit.pixelDistanceTravelling(base.heart)
        // Retreat into main
        + ?(base.naturalOf.filter(_.isOurs).exists(_.heart.altitude >= base.heart.altitude) && unit.battle.exists(_.enemy.attackCentroidGround.base.contains(base)), 32 * 40, 0))
      .map(_.heart.center)
      .getOrElse(With.geography.home.center))

  override def toString: String = destinations.filter(_.pixel.isDefined).reverseIterator.mkString("; ")
}

package Micro.Agency

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

abstract class DestinationStack(unit: FriendlyUnitInfo) {

  val destinations: Vector[Destination] = Destinations.All.map(new Destination(unit, _))
  var path: Option[TilePath] = None

  private def set(level: Int, p: Pixel)         : Destination = destinations(level).set(p)
  private def set(level: Int, t: Tile)          : Destination = set(level, t.center)
  private def set(level: Int, p: Option[Pixel]) : Destination = destinations(level).set(p)
  private def sett(level: Int, t: Option[Tile]) : Destination = set(level, t.map(_.center))

  val home      : Destination = destinations(Destinations.Home).set(unit.pixel)
  val redoubt   : Destination = destinations(Destinations.Redoubt)
  val terminus  : Destination = destinations(Destinations.Terminus).set(unit.pixel)
  val perch     : Destination = destinations(Destinations.Perch)
  val station   : Destination = destinations(Destinations.Station)
  val forced    : Destination = destinations(Destinations.Forced)
  val decision  : Destination = destinations(Destinations.Decision)

  def destinationsDefined : Seq[Destination]  = destinations.view.filter(_.pixel.isDefined)
  def origin              : Destination       = destinationsDefined.reverseIterator.find(_.level < 0).getOrElse(home)
  def destinationFinal    : Destination       = Maff.minBy(destinationsDefined)(d => ?(d.level < 0, 100, 0) + d.level).getOrElse(terminus)
  def destinationNext     : Destination       = Maff.minBy(destinationsDefined)(d => ?(d.level < 0, 100, 0) - d.level).getOrElse(terminus)

  def resetDestinations(): Unit = {
    destinations.foreach(_.clear())
    path = None
    home.set(defaultHome)
    terminus.set(unit.pixel)
    unit.stationDisengage                 .foreach(redoubt.set)
    unit.intent.toReturn                  .foreach(redoubt.set)
    unit.intent.toNuke                    .foreach(terminus.set)
    unit.intent.toAttack    .map(_.pixel) .foreach(terminus.set)
    unit.intent.toGather    .map(_.pixel) .foreach(terminus.set)
    unit.intent.toRepair    .map(_.pixel) .foreach(terminus.set)
    unit.intent.toFinish    .map(_.pixel) .foreach(terminus.set)
    unit.intent.toBoard     .map(_.pixel) .foreach(terminus.set)
    unit.intent.toBuildTile               .foreach(terminus.set)
    unit.intent.toScoutTiles.headOption   .foreach(terminus.set)
    unit.intent.toTravel                  .foreach(terminus.set)
    unit.stationEngage                    .foreach(station.set)
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
          || base == With.geography.ourNatural
          || base == With.geography.ourMain))(base =>
      unit.pixelDistanceTravelling(base.heart)
        // Retreat into main
        + ?(base.naturalOf.filter(_.isOurs).exists(_.heart.altitude >= base.heart.altitude) && unit.battle.exists(_.enemy.centroidGround.base.contains(base)), 32 * 40, 0))
      .map(_.heart.center)
      .getOrElse(With.geography.home.center))
}

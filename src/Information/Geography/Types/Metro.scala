package Information.Geography.Types

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

case class Metro(bases: Vector[Base]) extends Geo {
  def merge(other: Metro): Metro = Metro(bases ++ other.bases)

  val main    : Option[Base] = bases.find(_.isStartLocation)
  val natural : Option[Base] = main.flatMap(_.natural)
  val zones   : Vector[Zone] = With.geography.zones.filter(zone =>
    zone.bases.exists(bases.contains)
    || (zone.bases.isEmpty && {
      val center = zone.centroid.walkableTile
      With.paths.aStar(center, With.geography.startLocations.maxBy(_.groundTiles(center))).tiles.exists(_.exists(_.base.exists(bases.contains)))
    })
  )
  val tiles: Set[Tile] = zones.flatMap(_.tiles.view).toSet

  def units: Seq[UnitInfo] = zones.view.flatMap(_.units)

  def owner: PlayerInfo = {
    val owners = bases.view.map(_.owner).filterNot(_.isNeutral).distinct
    ?(owners.length == 1, owners.head, With.neutral)
  }

  lazy val heart: Tile = main.orElse(natural).map(_.heart).getOrElse(Maff.exemplarTiles(bases.map(_.heart)))
  lazy val isStartLocation: Boolean = bases.exists(_.isStartLocation)
}

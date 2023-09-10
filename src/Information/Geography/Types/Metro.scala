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
    zone.bases.forall(bases.contains)
    || (
      zone.bases.isEmpty
      && {
        val centroid = zone.centroid
        With.paths
          .aStar(centroid, With.geography.startLocations.maxBy(_.groundTiles(centroid)))
          .tiles
          .exists(_.exists(_.base.exists(bases.contains)))
      })
  )
  val tiles: Set[Tile] = zones.flatMap(_.tiles.view).toSet

  var name: String = ""

  def units: Seq[UnitInfo] = zones.view.flatMap(_.units)

  def owner: PlayerInfo = {
    val owners = bases.view.map(_.owner).filterNot(_.isNeutral).distinct
    ?(owners.length == 1, owners.head, With.neutral)
  }

  lazy val isStartLocation  : Boolean   = bases.exists(_.isStartLocation)
  lazy val heart            : Tile      = main.orElse(natural).map(_.heart).getOrElse(Maff.exemplarTiles(bases.map(_.heart)))

  override def toString: String = f"$arrow $name ${?(island, " Island", "")}${?(main.isDefined, " main", "")}: ${bases.map(_.toString).mkString(" >> ")}"
}

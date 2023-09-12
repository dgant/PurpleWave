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
  val zones   : Vector[Zone] = {
    val baseZones = bases.flatMap(_.zones)
    val pathZones = bases.flatMap(b1 => bases.flatMap(b2 => With.paths.aStar(b1.heart, b2.heart).tiles.getOrElse(Seq.empty).map(_.zone)))
    (baseZones ++ pathZones).distinct
  }
  val tiles: Set[Tile] = zones.flatMap(_.tiles.view).toSet

  var name: String = ""

  def units: Seq[UnitInfo] = zones.view.flatMap(_.units)

  def owner: PlayerInfo = {
    val owners = bases.view.map(_.owner).filterNot(_.isNeutral).distinct
    ?(owners.length == 1, owners.head, With.neutral)
  }

  lazy val heart: Tile = main.orElse(natural).map(_.heart).getOrElse(Maff.exemplarTiles(bases.map(_.heart)))

  override def toString: String = f"$arrow $name ${?(island, " Island", "")}${?(main.isDefined, " main", "")}: ${bases.map(_.toString).mkString(" >> ")}"
}

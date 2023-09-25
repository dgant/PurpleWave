package Information.Geography.Types

import Information.Geography.Calculations.{GetExits, ZonesConnecting}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

class Metro(private val seedBases: Vector[Base]) extends Geo {

  def merge(other: Metro): Metro = new Metro(seedBases ++ other.seedBases)

  lazy val zones: Vector[Zone] = {
    var output: Set[Zone] = Set.empty
    output ++= seedBases.flatMap(_.zones)
    output ++= seedBases.flatMap(b1 => seedBases.flatMap(b2 => ZonesConnecting(b1, b2)))

    var proceed = true
    while (proceed) {
      proceed = false

      val adjacentZones = output
        .flatMap(_.edges.flatMap(_.zones))
        .diff(output)
        .filterNot(z => z.isMain || z.isNatural)
        .filter(z =>
          output.map(_.groundDistance(z)).min
          <= With.geography.mains.filterNot(seedBases.contains).map(_.groundDistance(z)).min)

      adjacentZones
        .foreach(adjacentZone => {
        val plusAdjacent    = output + adjacentZone
        val areaBefore      = output.toSeq.map(_.tiles.size).sum
        val areaAfter       = areaBefore + adjacentZone.tiles.size
        val areaRatio       = Maff.nanToInfinity(areaAfter.toDouble / areaBefore)
        val exitsBefore     = GetExits(output)
        val exitsAfter      = GetExits(plusAdjacent)
        val perimeterBefore = exitsBefore .map(_.diameterPixels).sum
        val perimeterAfter  = exitsAfter  .map(_.diameterPixels).sum
        val perimeterRatio  = Maff.nanToInfinity(perimeterAfter / perimeterBefore)
        val basesBefore     = output.map(_.bases.size).sum
        val basesAfter      = plusAdjacent.toSeq.map(_.bases.size).sum
        val baseRatio       = basesAfter.toDouble / basesBefore

        if (areaRatio <= baseRatio && perimeterRatio <= baseRatio && exitsAfter.size <= exitsBefore.size) {
          proceed = true
          output += adjacentZone
        }
      })
    }
    output.toVector
  }
  lazy val bases    : Vector[Base]  = zones.flatMap(z => z.bases.filter(b => z.bases.length == 1 || seedBases.contains(b)))
  lazy val main     : Option[Base]  = seedBases.find(_.isMain)
  lazy val natural  : Option[Base]  = seedBases.find(_.isNatural)
  lazy val tiles    : Set[Tile]     = zones.flatMap(_.tiles.view).toSet
  lazy val heart    : Tile          = main.orElse(natural).map(_.heart).getOrElse(Maff.exemplarTiles(bases.map(_.heart)))

  var name: String = ""

  def units: Seq[UnitInfo] = zones.view.flatMap(_.units)

  def owner: PlayerInfo = {
    val owners = bases.view.map(_.owner).filterNot(_.isNeutral).distinct
    ?(owners.length == 1, owners.head, With.neutral)
  }

  override def toString: String = f"$arrow $name ${?(island, " Island", "")}${?(main.isDefined, " main", "")}: ${bases.map(_.toString).mkString(" >> ")}"
}

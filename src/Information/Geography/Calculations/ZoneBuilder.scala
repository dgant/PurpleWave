package Information.Geography.Calculations

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import Utilities.ByOption
import bwta.{BWTA, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

object ZoneBuilder {
  
  def zones: Iterable[Zone] = {
    With.grids.walkableTerrain.initialize()
    val names   = Random.shuffle(PlaceNames.countries)
    val nameQ   = new mutable.Queue[String] ++ names
    var repeats = 1
    val zones = BWTA.getRegions.asScala.map(region => {
      if (nameQ.isEmpty) {
        repeats += 1
        names.map(name => name + " " + repeats).foreach(name => nameQ.enqueue(name))
      }
      buildZone(region, nameQ.dequeue)
    })
    mapObviousTilesToZones(zones)
    zones
  }
  
  def edges: Iterable[Edge] = BWTA.getChokepoints.asScala.map(new Edge(_))
  def bases: Iterable[Base] = {
    val bases   = BaseFinder.calculate.map(new Base(_))
    val names   = Random.shuffle(PlaceNames.cities)
    val nameQ   = new mutable.Queue[String] ++ names
    var repeats = 1
    bases.foreach(base => {
      if (nameQ.isEmpty) {
        repeats += 1
        names.map(name => name + " " + repeats).foreach(name => nameQ.enqueue(name))
      }
      base.name = nameQ.dequeue
    })
    bases
  }
  
  def mapObviousTilesToZones(zones: Iterable[Zone]) {
    //This will map most -- but not all tiles
    With.geography.allTiles
      .filterNot(tile => zones.exists(_.contains(tile)))
      .foreach(assignTile(_, zones))
  }
  
  def assignTile(tile: Tile, zones: Iterable[Zone]): Boolean = {
    val neighborZones = Spiral
      .points(5)
      .map(tile.add)
      .filter(_.valid)
      .flatMap(neighborTile => zones.view.find(candidate => candidate.tiles.contains(neighborTile)))
    val consensusZone = ByOption.maxBy(neighborZones.groupBy(x => x))(_._2.size).map(_._1)
    consensusZone
      .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(tile)))
      .tiles
      .add(tile)
  }
  
  def buildZone(thisRegion: Region, name: String): Zone = {
    val tiles = With.geography.allTiles.filter(tile => BWTA.getRegion(tile.bwapi) == thisRegion)
    val x = tiles.map(_.x)
    val y = tiles.map(_.y)
    val boundingBox = TileRectangle(
      Tile(
        ByOption.min(x).getOrElse(0),
        ByOption.min(y).getOrElse(0)),
      Tile(
        ByOption.max(x).getOrElse(0),
        ByOption.max(y).getOrElse(0)))

    val tileSet = new mutable.HashSet[Tile]
    tileSet ++= tiles
    new Zone(name, thisRegion, boundingBox, tileSet)
  }
}

package Information.Geography.Calculations

import Mathematics.Shapes.Spiral
import Information.Geography.Types.{Base, Zone, ZoneEdge}
import ProxyBwapi.Races.Protoss
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import bwta.{BWTA, Chokepoint, Region}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ZoneBuilder {
  
  def build: Iterable[Zone] = {
    val zones = BWTA.getRegions.asScala.map(buildZone)
    val edges = BWTA.getChokepoints.asScala.map(choke => buildEdge(choke, zones))
    val bases = BaseFinder.calculate.map(townHallPixel => buildBase(townHallPixel, zones))
  
    bases.foreach(base => base.zone.bases += base)
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    zones.foreach(zone =>
      if (zone.edges.nonEmpty) {
        zone.exit =
          Some(zone.edges.minBy(edge =>
            With.geography.startLocations
              .map(_.groundPixels(edge.centerPixel))
              .max))
    })
    
    ensureAllTilesAreAssignedToAZone(zones)
    
    // We want to assign isNatural, which requires gas to be populated.
    // So let's run our regular ZoneUpdate now to populate the gas and then assign isNatural.
    zones.foreach(ZoneUpdater.updateZone)
    bases
      .filter(_.isStartLocation)
      .foreach(startLocationBase =>
        bases
          .filter(_.gas.nonEmpty)
          .minBy(
            _.townHallArea.startInclusive.groundPixels(
              startLocationBase.townHallArea.startInclusive))
          .isNaturalOf = Some(startLocationBase))
    
    zones
  }
  
  def ensureAllTilesAreAssignedToAZone(zones: Iterable[Zone]) {
    With.geography.allTiles
      .filterNot(tile => zones.exists(_.contains(tile)))
      .foreach(tile => assignTile(tile, zones))
  }
  
  def assignTile(tile: Tile, zones: Iterable[Zone]): Boolean = {
    val groundHeight = With.game.getGroundHeight(tile.bwapi)
    val candidates = zones.filter(_.altitude == groundHeight)
    val matchingZone = Spiral
      .points(5)
      .view
      .map(tile.add)
      .flatMap(neighborTile => candidates.view.find(candidate => candidate.tiles.contains(neighborTile)))
      .headOption
    matchingZone
      .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(tile)))
      .tiles
      .add(tile)
  }
  
  def buildZone(region: Region): Zone = {
    val polygon = region.getPolygon
    val tileArea = TileRectangle(
      Pixel(
        polygon.getPoints.asScala.map(_.getX).min,
        polygon.getPoints.asScala.map(_.getY).min).tileIncluding,
      Pixel(
        polygon.getPoints.asScala.map(_.getX).max,
        polygon.getPoints.asScala.map(_.getY).max).tileIncluding)
    val tiles = new mutable.HashSet[Tile]
    tiles ++= tileArea.tiles
      .filter(tile => {
        val tileRegion = BWTA.getRegion(tile.bwapi)
        tileRegion != null && tileRegion.getCenter == region.getCenter
      })
      .toSet
    val groundHeight = tiles.headOption.map(tile => With.game.getGroundHeight(tile.bwapi)).getOrElse(1)
    new Zone(
      region,
      groundHeight,
      tileArea,
      tiles,
      new ListBuffer[Base],
      new ListBuffer[ZoneEdge])
  }
  
  def buildBase(townHallTile: Tile, zones: Iterable[Zone]): Base = {
    val townHallArea = Protoss.Nexus.tileArea.add(townHallTile)
    new Base(
      zones
        .find(_.contains(townHallTile.pixelCenter))
        .getOrElse(zones.minBy(_.centroid.tileDistanceSquared(townHallTile))),
      townHallArea,
      With.geography.startLocations.exists(_.tileDistanceSlow(townHallTile) < 6))
  }
  
  def buildEdge(choke: Chokepoint, zones: Iterable[Zone]): ZoneEdge =
    new ZoneEdge(
      choke,
      Vector(
        choke.getRegions.first,
        choke.getRegions.second)
      .map(region => zones.minBy(_.centroid.pixelCenter.pixelDistanceSquared(new Pixel(region.getCenter)))))
}

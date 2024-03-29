package Information.Geography

import Information.Geography.Calculations.{FindBases, Labels, UpdateZones}
import Information.Geography.Types.{Base, Edge, LabelGenerator, Metro, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import Utilities.{SomeIf, SomeIfO}
import bwta.{BWTA, Region}

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

/**
  * Builds and publishes basic geographical features
  */
trait GeographyBuilder {
          val allTiles    : Vector[Tile]        = new TileRectangle(0, 0, With.mapTileWidth, With.mapTileHeight).tiles.toVector
  private var _edges      : Vector[Edge]        = Vector.empty
  private var _bases      : Vector[Base]        = Vector.empty
  private var _zones      : Vector[Zone]        = Vector.empty
  private var _metros     : Vector[Metro]       = Vector.empty
  private var _zoneByTile : Array[Zone]         = Array.empty
  private var _baseByTile : Array[Option[Base]] = Array.empty

  @inline final def edges   : Vector[Edge]  = _edges
  @inline final def bases   : Vector[Base]  = _bases
  @inline final def zones   : Vector[Zone]  = _zones
  @inline final def metros  : Vector[Metro] = _metros

  @inline final def zoneByTile      (tile: Tile)  : Zone          = _zoneByTile(Maff.clamp(tile.i, 0, _zoneByTile.length - 1))
  @inline final def zoneByValidTile (i: Int)      : Zone          = _zoneByTile(i)
  @inline final def zoneByValidTile (tile: Tile)  : Zone          = _zoneByTile(tile.i)
  @inline final def baseByTile      (tile: Tile)  : Option[Base]  = SomeIfO(tile.valid, _baseByTile(tile.i))
  @inline final def baseByValidTile (i: Int)      : Option[Base]  = _baseByTile(i)
  @inline final def baseByValidTile (tile: Tile)  : Option[Base]  = _baseByTile(tile.i)

  def onStart(): Unit = {
    With.grids.walkableTerrain.initialize()
    With.grids.walkableTerrain.update()
    With.grids.unwalkableUnits.initialize()
    With.grids.unwalkableUnits.update()

    val baseNames   = new LabelGenerator(Labels.cities)
    val zoneNames   = new LabelGenerator(Labels.countries)
    val metroNames  = new LabelGenerator(Labels.metropolitanAreas)

    // Build Zones
    //
    // Associate every tile with a BWEM region
    val regions = BWTA.getRegions.asScala
    val regionByTile = new Array[Region](allTiles.length)
    // Populate obvious regions
    allTiles.foreach(t => regionByTile(t.i) = BWTA.getRegion(t.bwapi))
    // Populate non-obvious regions
    regionByTile.indices.filter(regionByTile(_) == null).foreach(i => {
      val tile = new Tile(i)
      val neighborZones = Spiral(5).map(tile.add).filter(_.valid).map(_.i).map(regionByTile)
      regionByTile(i) = Maff.maxBy(neighborZones.groupBy(x => x))(_._2.size).map(_._1).getOrElse(regions.minBy(r => new Pixel(r.getCenter).tile.tileDistanceSquared(tile)))})
    _zones = regions.map(r => new Zone(zoneNames.next(), r, regionByTile.indices.filter(regionByTile(_) == r).map(new Tile(_)).toSet)).toVector
    _zoneByTile = allTiles.view.map(tile => zones.find(_.tiles.contains(tile)).get).toArray

    // Build Edges
    //
    _edges = BWTA.getChokepoints.asScala.map(new Edge(_)).toVector

    // Build Bases
    //
    val baseTiles = FindBases()
    val baseTilesByZone = baseTiles.groupBy(zoneByValidTile)
    val baseTileSets = baseTilesByZone.flatMap(p => p._2.map(base => (base, p._1.tiles.filter(tile => p._2.filterNot(base==).forall(_.tileDistanceSquared(tile) > base.tileDistanceSquared(tile))))))
    _bases = baseTileSets.map(p => new Base(baseNames.next(), p._1, p._2)).toVector
    _baseByTile = allTiles.view.map(t => _bases.find(_.tiles.contains(t))).toArray

    // We need zones and bases updated in order to construct metros
    //
    UpdateZones()

    // Build Metros
    //
    _metros = With.geography.mains.map(main => new Metro(Vector(main) ++ main.natural))

    With.geography.bases
      .filterNot(base => _metros.exists(_.bases.contains(base)))
      .sortBy   (base => With.geography.startLocations.map(_.groundPixels(base.heart)).min)
      .foreach(base =>
        if ( ! _metros.exists(_.bases.contains(base))) {
          _metros :+= new Metro(Vector(base))
        })

    var i = 0
    while(i < metros.length) {
      val metro = _metros(i)
      if (metro.main.isDefined) {
        i += 1
      } else {
        val closestMetro          = _metros.take(i).minBy(metroDistance(_, metro))
        val closestMetroDistance  = metroDistance(closestMetro, metro)
        if (closestMetroDistance < 32 * 30) {
          val j = _metros.indexOf(closestMetro)
          _metros = _metros.take(j) ++ Vector(closestMetro.merge(metro)) ++ _metros.drop(j + 1).filterNot(metro==)
        } else {
          i += 1
        }
      }
    }
    _metros.foreach(_.name = metroNames.next())
  }

  private def metroDistance(origin: Metro, other: Metro): Double = {
    val originBases = if (origin.main.isDefined) origin.main ++ origin.natural else origin.bases
    other.bases.view.flatMap(b => originBases.map(o => (o, b))).map(p => p._1.heart.groundPixels(p._2.heart)).min
  }
}

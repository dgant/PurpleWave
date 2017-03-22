package Information.Grids

import Geometry.Shapes.Square
import Information.Grids.Concrete.{GridUnits, _}
import Performance.Caching.{CacheFrame, Limiter}
import Startup.With
import bwapi.TilePosition
import Utilities.TypeEnrichment.EnrichPosition._

class Grids {
  val altitudeBonus = new GridAltitudeBonus
  val buildable = new GridBuildable
  val buildableTerrain = new GridBuildableTerrain
  val enemyDetection = new GridEnemyDetection
  val enemyStrength = new GridEnemyStrength
  val enemyVision = new GridEnemyVision
  val friendlyStrength = new GridFriendlyStrength
  val mobility = new GridMobility
  val units = new GridUnits
  val walkable = new GridWalkable
  val walkableTerran = new GridWalkableTerrain
  val walkableUnits = new GridWalkableUnits
  
  def onFrame() = limitUpdates.act()
  val limitUpdates = new Limiter(1, () => update)
  def update() {
    val tiles = relevantTiles
    
    List(
      altitudeBonus,
      units,
      enemyDetection,
      enemyStrength,
      enemyVision,
      friendlyStrength,
      buildable,
      buildableTerrain,
      walkable,
      walkableTerran,
      walkableUnits,
      mobility
    ).foreach(_.update(tiles))
  }
  
  def relevantTiles = relevantTilesCache.get
  private val relevantTilesCache = new CacheFrame[Set[TilePosition]](() => relevantTilesRecalculate)
  private def relevantTilesRecalculate:Set[TilePosition] = {
    val nearbyPoints = Square.points(18).toList
    With.units.ours.flatten(unit => nearbyPoints.map(unit.tileCenter.add))
  }
}

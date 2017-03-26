package Information.Grids

import Geometry.Shapes.Square
import Information.Grids.Concrete.{GridUnits, _}
import Performance.Caching.{Cache, Limiter}
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

class Grids {
  val altitudeBonus = new GridAltitudeBonus
  val buildable = new GridBuildable
  val buildableTerrain = new GridBuildableTerrain
  val enemyDetection = new GridEnemyDetection
  val enemyStrength = new GridEnemyStrength
  val enemyVision = new GridEnemyVision
  val friendlyVision = new GridFriendlyVision
  val friendlyStrength = new GridFriendlyStrength
  val mobility = new GridMobility
  val units = new GridUnits
  val walkable = new GridWalkable
  val walkableTerran = new GridWalkableTerrain
  val walkableUnits = new GridWalkableUnits
  
  val updateFrequency = 1
  
  def onFrame() = limitUpdates.act()
  val limitUpdates = new Limiter(updateFrequency, () => update)
  def update() {
    val tiles =
      if (With.units.ours.size > With.configuration.gridUpdateOptimizationUnitLimit)
        With.geography.allTiles
      else
        relevantTiles
    
    List(
      altitudeBonus,
      units,
      enemyDetection,
      enemyStrength,
      enemyVision,
      friendlyStrength,
      friendlyVision,
      buildable,
      buildableTerrain,
      walkable,
      walkableTerran,
      walkableUnits,
      mobility
    ).foreach(_.update(tiles))
  }
  
  def relevantTiles = relevantTilesCache.get
  private val relevantTilesCache = new Cache[Set[TilePosition]](updateFrequency, () => relevantTilesRecalculate)
  private def relevantTilesRecalculate:Set[TilePosition] = {
    val nearbyPoints = Square.points(18).toList
    With.units.ours.flatten(unit => nearbyPoints.map(unit.tileCenter.add))
  }
}

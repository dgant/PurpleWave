package Information.Grids.Dps

import ProxyBwapi.UnitClass.UnitClass
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{TilePosition, UnitSizeType}

class GridDpsEnemyToUnit {
  
  def get(
   tile:TilePosition,
   unit: UnitInfo): Double =
    get(tile, unit.unitClass)
  
  def get(
    tile:TilePosition,
    unitClass: UnitClass): Double = {
  
    val normal      = 1.0
    var concussive  = 1.0
    var explosive   = 1.0
  
    if (unitClass.size == UnitSizeType.Small) {
      explosive = 0.5
    }
    else if (unitClass.size == UnitSizeType.Medium) {
      concussive = 0.5
      explosive = 0.75
    }
    else if (unitClass.size == UnitSizeType.Large) {
      concussive = 0.25
    }
    
    if (unitClass.isFlyer)
      normal      * With.grids.dpsEnemyAirNormal.get(tile) +
      explosive   * With.grids.dpsEnemyAirExplosive.get(tile) +
      concussive  * With.grids.dpsEnemyAirConcussive.get(tile)
    else
      normal      * With.grids.dpsEnemyGroundNormal.get(tile) +
      explosive   * With.grids.dpsEnemyGroundExplosive.get(tile) +
      concussive  * With.grids.dpsEnemyGroundConcussive.get(tile)
  }
}

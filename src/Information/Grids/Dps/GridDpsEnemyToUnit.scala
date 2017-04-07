package Information.Grids.Dps

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{TilePosition, UnitSizeType}

class GridDpsEnemyToUnit {
  
  def get(
   tile:TilePosition,
   unit: UnitInfo): Double = {
    
    if ((unit.cloaked || unit.burrowed) && ! With.grids.enemyDetection.get(tile)) {
      return 0.0
    }
    
    //TODO: Account for shields taking full damage
    //TODO: Account for armor
  
    var concussive  = 1.0
    var explosive   = 1.0
  
    if (unit.unitClass.size == UnitSizeType.Small) {
      explosive = 0.5
    }
    else if (unit.unitClass.size == UnitSizeType.Medium) {
      concussive = 0.5
      explosive = 0.75
    }
    else if (unit.unitClass.size == UnitSizeType.Large) {
      concussive = 0.25
    }
    
    if (unit.flying)
                    With.grids.dpsEnemyAirNormal.get(tile) +
      explosive   * With.grids.dpsEnemyAirExplosive.get(tile) +
      concussive  * With.grids.dpsEnemyAirConcussive.get(tile)
    else
                    With.grids.dpsEnemyGroundNormal.get(tile) +
      explosive   * With.grids.dpsEnemyGroundExplosive.get(tile) +
      concussive  * With.grids.dpsEnemyGroundConcussive.get(tile)
  }
}

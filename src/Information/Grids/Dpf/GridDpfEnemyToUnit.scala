package Information.Grids.Dpf

import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.UnitSizeType

class GridDpfEnemyToUnit {
  
  def get(tile:Tile, unit: UnitInfo): Double = {
    
    if (unit.effectivelyCloaked) {
      return 0.0
    }
    
    //TODO: Account for armor
    
    var concussive  = 1.0
    var explosive   = 1.0
  
    if (unit.shieldPoints > 0) {
      // Everything deals full damage
    }
    else if (unit.unitClass.size == UnitSizeType.Small) {
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
                    With.grids.dpfEnemyAirNormal.get(tile) +
      explosive   * With.grids.dpfEnemyAirExplosive.get(tile) +
      concussive  * With.grids.dpfEnemyAirConcussive.get(tile)
    else
                    With.grids.dpfEnemyGroundNormal.get(tile) +
      explosive   * With.grids.dpfEnemyGroundExplosive.get(tile) +
      concussive  * With.grids.dpfEnemyGroundConcussive.get(tile)
  }
}

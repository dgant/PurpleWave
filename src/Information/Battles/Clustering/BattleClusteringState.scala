package Information.Battles.Clustering

import Lifecycle.With
import Mathematics.Pixels.Tile
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(units: Traversable[UnitInfo]) {
  
          val clusters        = new ArrayBuffer[ArrayBuffer[UnitInfo]]
  private val unassignedUnits = mutable.HashSet.empty ++ units
  private val exploredTiles   = new mutable.HashSet[Tile]
  private val horizonTiles    = new mutable.HashSet[Tile]
  
  def isComplete:Boolean = unassignedUnits.isEmpty
  
  def step() {
    if (unassignedUnits.isEmpty) return
  
    val nextUnit = unassignedUnits.head
  
    val nextCluster = new ArrayBuffer[UnitInfo]
    unassignedUnits   -= nextUnit
    nextCluster       += nextUnit
  
    horizonTiles.add(nextUnit.tileIncludingCenter)
  
    while (horizonTiles.nonEmpty) {
    
      val nextTile = horizonTiles.head
      horizonTiles  -=  nextTile
      exploredTiles +=  nextTile
    
      val nextUnits = unitsInTile(nextTile)
      unassignedUnits   --= nextUnits
      nextCluster       ++= nextUnits
    
      //Note that this includes non-combatants!
      Circle.points(With.configuration.battleMarginTiles)
        .foreach(point => {
          val tile = nextTile.add(point)
          if (tile.valid
            && ! exploredTiles.contains(tile)
            && unitsInTile(tile).exists(unassignedUnits.contains))
            horizonTiles += tile
        })
    }
  
    clusters.append(nextCluster)
  }
  
  private def unitsInTile(tile:Tile) = With.grids.units.get(tile).filter(unassignedUnits.contains)
}

package Information.Grids.Abstract

import Geometry.Shapes.Circle
import Micro.Battles.BattleMetrics
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

abstract class GridStrength extends GridDouble {
  
  private val rangeMargin = 48
  private val framesToLookAhead = 48
  
  override def update(relevantTiles:Iterable[TilePosition]) {
    reset(relevantTiles)
    getUnits.foreach(unit => {
      val strength = BattleMetrics.estimateStrength(unit)
      val latencyFrames = With.game.getLatencyFrames
      val tilePosition = unit.pixelCenter.toTilePosition //position.toTilePosition uses the unit's center rather than its top-left corner
      val rangeFull = unit.unitClass.maxAirGroundRange
      val rangeZero = unit.unitClass.maxAirGroundRange + rangeMargin + (unit.unitClass.topSpeed * (framesToLookAhead + latencyFrames)).toInt
      if (strength > 0) {
        populate(tilePosition, rangeFull, rangeZero, strength)
      }
    })
  }
  
  private def populate(tile:TilePosition, distanceFull:Int, distanceZero:Int, strength:Double) {
    Circle.points(distanceZero/32).foreach(point => {
      val nearbyTile = tile.add(point)
      val distance = Math.sqrt(32 * 32 * point.lengthSquared)
      val ratio = Math.min(1, Math.max(0, (distanceZero - distance) / (distanceZero - distanceFull)))
      add(nearbyTile, (strength * ratio).toInt)
    })
  }
  
  protected def getUnits:Iterable[UnitInfo]
}

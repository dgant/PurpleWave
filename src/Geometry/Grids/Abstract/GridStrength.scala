package Geometry.Grids.Abstract

import Geometry.Circle
import Global.Combat.Battle.BattleMetrics
import Types.UnitInfo.UnitInfo
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

abstract class GridStrength extends GridInt {
  
  val _limitUpdates = new Limiter(4, _update)
  override def update() {
    _limitUpdates.act()
  }
  
  def _update() {
    reset()
    _getUnits.foreach(unit => {
      val rangeMarginFull = 32
      val rangeMarginHalf = 32 * 3
      val strengthHalf = BattleMetrics.evaluate(unit) / 2
      val tilePosition = unit.position.toTilePosition //position.toTilePosition uses the unit's center rather than its top-left corner
      _populate(tilePosition, (unit.range + rangeMarginFull)/32, strengthHalf)
      _populate(tilePosition, (unit.range + rangeMarginHalf)/32, strengthHalf)
    })
  }
  
  def _populate(tilePosition:TilePosition, tileRange:Int, strength:Int) {
    Circle.points(tileRange).foreach(point => add(tilePosition.add(point._1, point._2), strength))
  }
  
  def _getUnits:Iterable[UnitInfo]
}

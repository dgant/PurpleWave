package Geometry.Grids.Abstract

import Geometry.Circle
import Global.Combat.Battle.BattleMetrics
import Types.UnitInfo.UnitInfo
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._

abstract class GridStrength extends GridInt {
  
  val _limitUpdates = new Limiter(4, _update)
  override def update() {
    _limitUpdates.act()
  }
  
  def _update() {
    reset()
    _getUnits.foreach(unit => {
      val strength = BattleMetrics.evaluate(unit)
      val tilePosition = unit.position.toTilePosition //position.toTilePosition uses the unit's center rather than its top-left corner
      Circle.points((unit.range + 16)/32).foreach(point =>
        add(tilePosition.add(point._1, point._2), strength))
    })
  }
  
  def _getUnits:Iterable[UnitInfo]
}

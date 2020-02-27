package Information.Grids.Combat

import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyRangeAirGround extends AbstractGridEnemyRange {
  override protected def pixelRangeMax(unit: UnitInfo): Double =
    if (unit.canAttack)
      Math.max(unit.pixelRangeGround, unit.pixelRangeAir)
    else
      0.0
}

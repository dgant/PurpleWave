package Information.Grids.Combat

import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyRangeAir extends AbstractGridEnemyRange {
  override protected def pixelRangeMax(unit: UnitInfo): Double =
    if (unit.canAttack && unit.attacksAgainstAir > 0)
      unit.pixelRangeAir
    else
      0.0
}

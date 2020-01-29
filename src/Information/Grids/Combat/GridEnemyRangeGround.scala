package Information.Grids.Combat
import ProxyBwapi.UnitInfo.UnitInfo

class GridEnemyRangeGround extends AbstractGridEnemyRange {
  override protected def pixelRangeMax(unit: UnitInfo): Double =
    if (unit.canAttack && unit.attacksAgainstGround > 0)
      unit.pixelRangeGround
    else
      0.0
}

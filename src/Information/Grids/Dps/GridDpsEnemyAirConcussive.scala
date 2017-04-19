package Information.Grids.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyAirConcussive extends AbstractGridDpsEnemy {
  
  override protected val air: Boolean = true
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.airDamageTypeRaw == DamageType.Concussive)
  
}

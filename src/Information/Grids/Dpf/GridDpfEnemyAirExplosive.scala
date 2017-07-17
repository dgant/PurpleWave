package Information.Grids.Dpf

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpfEnemyAirExplosive extends AbstractGridDpfEnemy {
  
  override protected val air: Boolean = true
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.airDamageTypeRaw == DamageType.Explosive)
  
}

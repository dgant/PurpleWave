package Information.Grids.Dpf

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpfEnemyGroundExplosive extends AbstractGridDpfEnemy {
  
  override protected val air: Boolean = false
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.groundDamageTypeRaw == DamageType.Explosive)
  
}

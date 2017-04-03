package Information.Grids.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyAirExplosive extends AbstractGridDpsEnemy {
  
  override protected val air: Boolean = true
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.rawAirDamageType == DamageType.Explosive)
  
}

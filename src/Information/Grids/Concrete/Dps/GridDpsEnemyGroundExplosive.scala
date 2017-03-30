package Information.Grids.Concrete.Dps

import Information.Grids.Abstract.Dps.GridDpsEnemy
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyGroundExplosive extends GridDpsEnemy {
  
  override protected val air: Boolean = false
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.rawAirDamageType == DamageType.Explosive)
  
}

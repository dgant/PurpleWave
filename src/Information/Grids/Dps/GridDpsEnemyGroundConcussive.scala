package Information.Grids.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyGroundConcussive extends AbstractGridDpsEnemy {
  
  override protected val air: Boolean = false
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.rawGroundDamageType == DamageType.Concussive)
  
}

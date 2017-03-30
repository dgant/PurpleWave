package Information.Grids.Concrete.Dps

import Information.Grids.Abstract.Dps.GridDpsEnemy
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyAirConcussive extends GridDpsEnemy {
  
  override protected val air: Boolean = true
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filter(_.unitClass.rawAirDamageType == DamageType.Concussive)
  
}

package Information.Grids.Concrete.Dps

import Information.Grids.Abstract.Dps.GridDpsEnemy
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyAirNormal extends GridDpsEnemy {
  
  val unacceptableDamageTypes = List(DamageType.Concussive, DamageType.Explosive)
  override protected val air: Boolean = true
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filterNot(u => unacceptableDamageTypes.contains(u.unitClass.rawAirDamageType))
  
}

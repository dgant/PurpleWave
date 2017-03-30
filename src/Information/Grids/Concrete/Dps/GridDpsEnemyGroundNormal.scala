package Information.Grids.Concrete.Dps

import Information.Grids.Abstract.Dps.GridDpsEnemy
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyGroundNormal extends GridDpsEnemy {
  
  val unacceptableDamageTypes = List(DamageType.Concussive, DamageType.Explosive)
  override protected val air: Boolean = false
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filterNot(u => unacceptableDamageTypes.contains(u.unitClass.rawGroundDamageType))
  
}

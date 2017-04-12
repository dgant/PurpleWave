package Information.Grids.Dps

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpsEnemyGroundNormal extends AbstractGridDpsEnemy {
  
  val unacceptableDamageTypes = Vector(DamageType.Concussive, DamageType.Explosive)
  override protected val air: Boolean = false
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filterNot(u => unacceptableDamageTypes.contains(u.unitClass.rawGroundDamageType))
  
}

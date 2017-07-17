package Information.Grids.Dpf

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpfEnemyGroundNormal extends AbstractGridDpfEnemy {
  
  val unacceptableDamageTypes = Vector(DamageType.Concussive, DamageType.Explosive)
  override protected val air: Boolean = false
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filterNot(u => unacceptableDamageTypes.contains(u.unitClass.groundDamageTypeRaw))
  
}

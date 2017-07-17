package Information.Grids.Dpf

import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.DamageType

class GridDpfEnemyAirNormal extends AbstractGridDpfEnemy {
  
  val unacceptableDamageTypes = Vector(DamageType.Concussive, DamageType.Explosive)
  override protected val air: Boolean = true
  override protected def getUnits: Iterable[UnitInfo] =
    super.getUnits.filterNot(u => unacceptableDamageTypes.contains(u.unitClass.airDamageTypeRaw))
  
}

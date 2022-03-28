package Planning.Plans.Macro.Protoss

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class BuildShieldBatteriesAtNatural(initialCount: Int) extends BuildTowersAtBases(initialCount, towerClass = Protoss.ShieldBattery) {
  
  override def eligibleBases: Vector[Base] = {
    var output = With.geography.ourBasesAndSettlements.filter(With.geography.ourNatural==)
    if (output.isEmpty) {
      output = Vector(With.geography.ourNatural)
    }
    output
  }
}

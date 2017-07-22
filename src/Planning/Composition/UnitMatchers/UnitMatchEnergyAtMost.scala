package Planning.Composition.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchEnergyAtMost(energy: Int) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = {
    unit.energy <= energy
  }
  
}

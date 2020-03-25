package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchEnergyAtMost(energy: Int) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = {
    unit.energy <= energy
  }
  
}

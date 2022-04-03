package Utilities.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchEnergyAtMost(energy: Int) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = {
    unit.energy <= energy
  }
  
}

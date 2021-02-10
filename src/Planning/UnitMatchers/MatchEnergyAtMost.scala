package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchEnergyAtMost(energy: Int) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = {
    unit.energy <= energy
  }
  
}

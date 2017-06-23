package Planning.Composition.UnitMatchers
import Information.Battles.Estimation.Avatar
import ProxyBwapi.UnitInfo.UnitInfo

class UnitMatchBattle(enemy: Avatar) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = {
    
    // TODO: Exclude unhelpful stuff, like Corsairs vs. Zerglings
    
    unit.unitClass.helpsInCombat
  }
}

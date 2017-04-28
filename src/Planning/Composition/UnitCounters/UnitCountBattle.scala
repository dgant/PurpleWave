package Planning.Composition.UnitCounters
import Information.Battles.BattleTypes.Battle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitCountBattle(battle: Battle) extends UnitCounter {
  
  override def continue(units: Iterable[FriendlyUnitInfo]): Boolean = {
    
    //Specify an overkill ratio of, say 1.5
    //Keep accepting units until we exceed that ratio
    
    true
  }
  
  override def accept(units: Iterable[FriendlyUnitInfo]): Boolean = {
    
    // Accept units if the battle is at home or if the ratio > 1.0
    
    true
  }
}

package Planning.Plans.Information

import Planning.Plans.Compound.{And, IfThenElse, Not}
import Planning.Plans.Macro.UnitCount.SupplyAtLeast

class ScoutAt(minimumSupply:Int) extends IfThenElse {
  
  description.set("Send a scout at a specific supply count")
  
  predicate.set(
    new And { children.set(List(
      new SupplyAtLeast { quantity.set(minimumSupply/2) },
      new Not { child.set(new FoundEnemyBase) }
    ))})
    
  whenTrue.set(new FindEnemyBase)
}

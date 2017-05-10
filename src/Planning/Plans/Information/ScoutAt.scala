package Planning.Plans.Information

import Planning.Plans.Compound.{And, IfThenElse, Not}
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class ScoutAt(minimumSupply:Int) extends IfThenElse {
  
  description.set("Send a scout at a specific supply count")
  
  predicate.set(
    new And { children.set(Vector(
      new SupplyAtLeastDoubleThis { quantity.set(minimumSupply) },
      new Not { child.set(new FoundEnemyBase) }
    ))})
    
  whenTrue.set(new FindEnemyBase)
}

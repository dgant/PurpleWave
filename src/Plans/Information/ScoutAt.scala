package Plans.Information

import Plans.Compound.{And, IfThenElse, Not}
import Plans.Macro.UnitCount.SupplyAtLeast

class ScoutAt(minimumSupply:Int) extends IfThenElse {
  description.set(Some("Send a scout at a specific supply count"))
  predicate.set(
    new And { children.set(List(
      new SupplyAtLeast { quantity.set(minimumSupply) },
      new Not { child.set(new KnowEnemyBase) }
    ))})
    
  whenTrue.set(new FindEnemyBase)
}

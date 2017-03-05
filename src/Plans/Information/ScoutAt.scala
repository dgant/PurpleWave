package Plans.Information

import Plans.Compound.{IfThenElse}
import Plans.Macro.UnitCount.SupplyAtLeast

class ScoutAt(minimumSupply:Int) extends IfThenElse {
  description.set(Some("Send a scout at a specific supply count"))
  predicate.set(new SupplyAtLeast { quantity.set(minimumSupply) })
  whenTrue.set(new FindEnemyBase)
}

package Plans.Information

import Plans.Compound.While
import Plans.Macro.UnitCount.SupplyAtLeast

class ScoutAt(minimumSupply:Int) extends While{
  description.set(Some("Send a scout at a specific supply count"))
  predicate.set(new SupplyAtLeast { quantity.set(minimumSupply) })
  action.set(new FindEnemyBase)
}

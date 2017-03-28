package Planning.Plans.Macro

import Planning.Plans.Compound.IfThenElse
import Planning.Plans.Macro.UnitCount.SupplyAtLeast

class RemoveMineralBlockAt(minimumSupply:Int) extends IfThenElse {
  
  description.set("Remove a mineral block at a specific supply count")
  
  predicate.set(new SupplyAtLeast { quantity.set(minimumSupply*2) })
  whenTrue.set(new RemoveMineralBlocks)
}

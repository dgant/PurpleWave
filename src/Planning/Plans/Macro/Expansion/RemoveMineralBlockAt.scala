package Planning.Plans.Macro.Expansion

import Planning.Plans.Compound.IfThenElse
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class RemoveMineralBlockAt(minimumSupply:Int) extends IfThenElse {
  
  description.set("Remove a mineral block at a specific supply count")
  
  predicate.set(new SupplyAtLeastDoubleThis { quantity.set(minimumSupply) })
  whenTrue.set(new RemoveMineralBlocks)
}

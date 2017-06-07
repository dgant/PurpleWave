package Planning.Plans.Macro.Expansion

import Planning.Plans.Compound.IfThenElse
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class RemoveMineralBlockAt(minimumSupply:Int)
  extends IfThenElse(
    new SupplyAtLeastDoubleThis(minimumSupply),
    new RemoveMineralBlocks) {
  
  description.set("Remove a mineral block at " + minimumSupply + " supply")
}

package Planning.Plans.Macro.Expanding

import Planning.Plans.Compound.If
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class RemoveMineralBlockAt(minimumSupply:Int)
  extends If(
    new SupplyAtLeastDoubleThis(minimumSupply),
    new RemoveMineralBlocks) {
  
  description.set("Remove a mineral block at " + minimumSupply + " supply")
}

package Planning.Plans.Macro.Expanding

import Planning.Plans.Compound.If
import Planning.Plans.Predicates.Milestones.SupplyOutOf200

class RemoveMineralBlocksAt(minimumSupply:Int)
  extends If(
    new SupplyOutOf200(minimumSupply),
    new RemoveMineralBlocks) {
  
  description.set("Remove a mineral block at " + minimumSupply + " supply")
}

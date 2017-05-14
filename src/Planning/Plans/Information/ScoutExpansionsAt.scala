package Planning.Plans.Information

import Planning.Plans.Compound.IfThenElse
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class ScoutExpansionsAt(minimumSupply:Int) extends IfThenElse(
  new SupplyAtLeastDoubleThis(minimumSupply),
  new FindExpansions) {
  
  description.set("Scout expansions at " + minimumSupply + " supply")
}

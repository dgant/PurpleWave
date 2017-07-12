package Planning.Plans.Scouting

import Planning.Plans.Compound.If
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class ScoutExpansionsAt(minimumSupply: Int)
  extends If(
    new SupplyAtLeastDoubleThis(minimumSupply),
    new FindExpansions) {
  
  description.set("Monitor enemy expansions at " + minimumSupply + " supply")
}

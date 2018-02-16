package Planning.Plans.Scouting

import Planning.Plans.Compound.If
import Planning.Plans.Predicates.Milestones.SupplyOutOf200

class ScoutExpansionsAt(minimumSupply: Int)
  extends If(
    new SupplyOutOf200(minimumSupply),
    new FindExpansions) {
  
  description.set("Monitor enemy expansions at " + minimumSupply + " supply")
}

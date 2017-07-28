package Planning.Plans.Scouting

import Planning.Plans.Compound.If
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class ScoutAt(minimumSupply: Int) extends If(
  new SupplyAtLeastDoubleThis(minimumSupply),
  new Scout) {
  
  description.set("Find an enemy base at " + minimumSupply + " supply")
}

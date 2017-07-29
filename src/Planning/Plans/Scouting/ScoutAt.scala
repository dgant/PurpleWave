package Planning.Plans.Scouting

import Planning.Plans.Compound.If
import Planning.Plans.Macro.Milestones.SupplyAtLeastDoubleThis

class ScoutAt(minimumSupply: Int, scoutCount: Int = 1) extends If(
  new SupplyAtLeastDoubleThis(minimumSupply),
  new Scout(scoutCount)) {
  
  description.set("Scout at " + minimumSupply + " supply")
}

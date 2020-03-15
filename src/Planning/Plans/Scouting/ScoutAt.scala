package Planning.Plans.Scouting

import Planning.Plans.Compound.If
import Planning.Predicates.Milestones.SupplyOutOf200

class ScoutAt(minimumSupply: Int, scoutCount: Int = 1) extends If(
  new SupplyOutOf200(minimumSupply),
  new ScoutWithWorkers(scoutCount)) {
  
  description.set("Scout at " + minimumSupply + " supply")
}

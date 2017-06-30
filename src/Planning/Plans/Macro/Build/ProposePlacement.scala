package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plan

class ProposePlacement(buildingDescriptor: Blueprint) extends Plan {
  
  description.set("Propose placing " + buildingDescriptor)
  
  override def onUpdate() {
    With.groundskeeper.propose(buildingDescriptor)
  }
}

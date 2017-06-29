package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Planning.Plan

class ProposePlacement(buildingDescriptor: BuildingDescriptor) extends Plan {
  
  description.set("Propose placing " + buildingDescriptor)
  
  override def onUpdate() {
    With.groundskeeper.propose(buildingDescriptor)
  }
}

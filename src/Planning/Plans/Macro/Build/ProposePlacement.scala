package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Planning.Plan

class ProposePlacement(buildingDescriptors: BuildingDescriptor*) extends Plan {
  
  description.set("Propose placing "
    + buildingDescriptors.take(3).map(_.toString).mkString(", ")
    + (if(buildingDescriptors.size > 3) "..." else ""))
  
  override def onUpdate() {
    buildingDescriptors.foreach(buildingDescriptor =>
      With.groundskeeper.suggest(buildingDescriptor))
  }
}

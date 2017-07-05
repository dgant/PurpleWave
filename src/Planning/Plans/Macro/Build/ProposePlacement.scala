package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plan

class ProposePlacement extends Plan {
  
  description.set("Propose blueprints")
  
  lazy val blueprints: Iterable[Blueprint] = Iterable.empty
  
  override def onUpdate() {
    blueprints.foreach(With.groundskeeper.propose)
  }
}

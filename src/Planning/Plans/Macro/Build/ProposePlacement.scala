package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plan

class ProposePlacement(initialBlueprints: Blueprint*) extends Plan {
  
  description.set("Propose blueprints")
  
  lazy val blueprints: Seq[Blueprint] = initialBlueprints
  
  override def onUpdate() {
    blueprints.foreach(With.groundskeeper.propose)
  }
}

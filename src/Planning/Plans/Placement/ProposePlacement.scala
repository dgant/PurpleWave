package Planning.Plans.Placement

import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plan

class ProposePlacement(initialBlueprints: Blueprint*) extends Plan {
  
  lazy val blueprints: Seq[Blueprint] = initialBlueprints
  
  override def onUpdate() {
    blueprints.foreach(With.groundskeeper.suggest)
  }
}

package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Techs.Tech

class EnemyHasTech(tech:Tech) extends Plan {
  
  description.set("Require enemy to have a tech")
  
  override def isComplete: Boolean = With.enemies.exists(_.hasResearched(tech))
}

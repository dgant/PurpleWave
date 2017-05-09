package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Techs.Tech

class HaveTech(tech:Tech) extends Plan {
  
  description.set("Require a tech")
  
  override def isComplete: Boolean = With.self.hasResearched(tech)
}

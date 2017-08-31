package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Techs.Tech

class TechComplete(tech: Tech) extends Plan {
  
  description.set("Require a tech")
  
  override def isComplete: Boolean = With.self.hasTech(tech)
}

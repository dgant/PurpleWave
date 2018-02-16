package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Techs.Tech

class TechComplete(tech: Tech, withinFrames: Int = 0) extends Plan {
  
  description.set("Require a tech")
  
  override def isComplete: Boolean = (
    With.self.hasTech(tech)
    || (withinFrames >= 0 && With.units.ours.exists(unit =>
        unit.teching
        && unit.techingType == tech
        && unit.framesBeforeTechComplete <= withinFrames)))
}

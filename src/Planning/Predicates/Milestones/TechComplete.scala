package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Techs.Tech

class TechComplete(tech: Tech, withinFrames: Int = 0) extends Predicate {
  
  override def apply: Boolean = (
    With.self.hasTech(tech)
    || (withinFrames >= 0 && With.units.ours.exists(unit =>
        unit.teching
        && unit.techingType == tech
        && unit.remainingTechFrames <= withinFrames)))
}

package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import ProxyBwapi.Techs.Tech

case class TechComplete(tech: Tech, withinFrames: Int = 0) extends Predicate {
  override def apply: Boolean = MacroFacts.techComplete(tech, withinFrames)
}

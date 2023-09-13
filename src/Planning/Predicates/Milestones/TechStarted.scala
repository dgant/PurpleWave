package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import ProxyBwapi.Techs.Tech

case class TechStarted(tech: Tech) extends Predicate {
  override def apply: Boolean = MacroFacts.techStarted(tech)
}
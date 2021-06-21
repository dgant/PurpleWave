package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import ProxyBwapi.Techs.Tech

case class TechStarted(tech: Tech) extends Predicate {
  override def apply: Boolean = MacroFacts.techStarted(tech)
}
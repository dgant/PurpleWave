package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import ProxyBwapi.Techs.Tech

case class GasForTech(tech: Tech) extends Predicate {
  override def apply: Boolean = MacroFacts.haveGasForTech(tech)
}
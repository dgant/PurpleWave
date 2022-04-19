package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import ProxyBwapi.Techs.Tech

case class GasForTech(tech: Tech) extends Predicate {
  override def apply: Boolean = MacroFacts.haveGasForTech(tech)
}
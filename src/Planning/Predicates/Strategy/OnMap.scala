package Planning.Predicates.Strategy

import Planning.Predicates.{MacroFacts, Predicate}
import Strategery.StarCraftMap

case class OnMap(maps: StarCraftMap*) extends Predicate {
  override def apply: Boolean = MacroFacts.onMap(maps: _*)
}

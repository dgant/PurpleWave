package Planning.Predicates.Strategy

import Planning.Predicate
import Planning.Predicates.MacroFacts
import Strategery.StarCraftMap

case class OnMap(maps: StarCraftMap*) extends Predicate {
  override def apply: Boolean = MacroFacts.onMap(maps: _*)
}

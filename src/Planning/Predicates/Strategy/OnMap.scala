package Planning.Predicates.Strategy

import Planning.MacroFacts
import Planning.Predicates.Predicate
import Strategery.StarCraftMap

case class OnMap(maps: StarCraftMap*) extends Predicate {
  override def apply: Boolean = MacroFacts.onMap(maps: _*)
}

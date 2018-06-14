package Planning.Predicates

import Planning.Predicate
import Strategery.StarCraftMap

class OnMap(map: StarCraftMap) extends Predicate {
  
  override def isComplete: Boolean = map.matches
  
}

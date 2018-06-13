package Planning.Plans.Predicates

import Planning.Plan
import Strategery.StarCraftMap

class OnMap(map: StarCraftMap) extends Plan {
  
  override def isComplete: Boolean = map.matches
  
}

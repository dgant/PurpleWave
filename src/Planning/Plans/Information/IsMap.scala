package Planning.Plans.Information

import Planning.Plan
import Strategery.Maps.StarCraftMap

class IsMap(map: StarCraftMap) extends Plan {
  
  override def isComplete: Boolean = map.matches
  
}

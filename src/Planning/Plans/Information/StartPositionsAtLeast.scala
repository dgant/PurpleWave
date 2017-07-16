package Planning.Plans.Information

import Lifecycle.With
import Planning.Plan

class StartPositionsAtLeast(count: Int) extends Plan {
  
  override def isComplete: Boolean = {
    With.geography.startLocations.size >= count
  }
}

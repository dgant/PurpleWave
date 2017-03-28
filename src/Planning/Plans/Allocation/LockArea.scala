package Planning.Plans.Allocation

import Geometry.{Positions, TileRectangle}
import Planning.Plan
import Startup.With

class LockArea extends Plan {
  
  description.set("Reserve an area")
  
  var area = new TileRectangle(Positions.tileMiddle, Positions.tileMiddle)
  
  private var isSatisfied = false
  
  override def isComplete:Boolean = isSatisfied
  override def onFrame() = isSatisfied = With.reservations.request(this, area)
}

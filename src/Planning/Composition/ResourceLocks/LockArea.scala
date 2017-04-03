package Planning.Composition.ResourceLocks

import Geometry.{Positions, TileRectangle}
import Planning.Plan
import Startup.With

class LockArea extends ResourceLock {
  
  var area = new TileRectangle(Positions.tileMiddle, Positions.tileMiddle)
  var owner:Plan = null
  
  private var isSatisfied = false
  
  override def isComplete:Boolean = isSatisfied
  override def acquire(plan: Plan) = {
    owner = plan
    isSatisfied = With.reservations.request(plan, area)
  }
  
  override def release {
    throw new NotImplementedError
  }
}

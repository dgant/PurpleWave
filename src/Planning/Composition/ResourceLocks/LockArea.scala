package Planning.Composition.ResourceLocks

import Planning.Plan
import Lifecycle.With
import Mathematics.Pixels.TileRectangle

class LockArea extends ResourceLock {
  
  var area:Option[TileRectangle] = None
  var owner:Plan = null
  
  private var isSatisfied = false
  
  override def satisfied:Boolean = isSatisfied
  
  override def acquire(plan: Plan) = {
    owner = plan
    isSatisfied = With.realEstate.request(this)
  }
  
  override def release() {
    With.realEstate.release(this)
    isSatisfied = false
  }
}

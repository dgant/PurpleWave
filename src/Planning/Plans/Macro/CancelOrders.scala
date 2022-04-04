package Planning.Plans.Macro

import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Utilities.UnitFilters.{IsAny, UnitFilter}

class CancelOrders(matchers: UnitFilter*) extends Plan {

  val lock = new LockUnits(this)
  lock.matcher = IsAny(matchers: _*)

  override def onUpdate(): Unit = {
    lock.acquire()
    lock.units.foreach(_.intend(this, new Intention {
      shouldCancel = true
    }))
  }
}

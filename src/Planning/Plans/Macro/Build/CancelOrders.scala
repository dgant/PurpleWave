package Planning.Plans.Macro.Build

import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.{MatchOr, UnitMatcher}

class CancelOrders(matchers: UnitMatcher*) extends Plan {

  val lock = new LockUnits(this)
  lock.matcher = MatchOr(matchers: _*)

  override def onUpdate(): Unit = {
    lock.acquire()
    lock.units.foreach(_.intend(this, new Intention {
      shouldCancel = true
    }))
  }
}

package Planning.Plans.Macro.Build

import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.{MatchOr, UnitMatcher}

class CancelOrders(matchers: UnitMatcher*) extends Plan {

  val lock = new LockUnits
  lock.matcher = MatchOr(matchers: _*)

  override def onUpdate(): Unit = {
    lock.acquire(this)
    lock.units.foreach(_.agent.intend(this, new Intention {
      canCancel = true
    }))
  }
}

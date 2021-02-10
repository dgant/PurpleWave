package Planning.Plans.Macro.Build

import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.{MatchOr, Matcher}

class CancelOrders(matchers: Matcher*) extends Plan {

  val lock = new LockUnits
  lock.unitMatcher.set(MatchOr(matchers: _*))

  override def onUpdate(): Unit = {
    lock.acquire(this)
    lock.units.foreach(_.agent.intend(this, new Intention {
      canCancel = true
    }))
  }
}

package Planning.Plans.Macro.Build

import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.{UnitMatchOr, UnitMatcher}

class CancelAll(matchers: UnitMatcher*) extends Plan {

  val lock = new LockUnits
  lock.unitMatcher.set(UnitMatchOr(matchers: _*))

  override def onUpdate(): Unit = {
    lock.acquire(this)
    lock.units.foreach(_.agent.intend(this, new Intention {
      canCancel = true
    }))
  }
}

package Tactic.Tactics

import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountEverything

class DoFloatBuildings extends Tactic {

  val floaties: LockUnits = new LockUnits(this)
  floaties.counter = CountEverything

  def launch(): Unit = {
    floaties.matcher = u => ! u.flying && With.blackboard.floatableBuildings().exists(_(u))
    floaties.acquire()
    floaties.units.foreach(_.intend(this).setShouldLiftoff())
  }
}
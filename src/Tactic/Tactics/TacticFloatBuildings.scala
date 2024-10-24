package Tactic.Tactics

import Lifecycle.With
import Planning.ResourceLocks.LockUnits

class TacticFloatBuildings extends Tactic {

  val floaties: LockUnits = new LockUnits(this, u => ! u.flying && With.blackboard.floatableBuildings().exists(_(u)))

  def launch(): Unit = {
    floaties.acquire().foreach(_.intend(this).setShouldLiftoff())
  }
}
package Tactic.Missions

import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountBetween
import Utilities.UnitFilters.{IsAll, IsAny}
import ProxyBwapi.Races.Protoss

class MissionRecall extends Mission {
  override def shouldForm: Boolean = Protoss.Recall(With.self) && arbiterLock.inquire().exists(_.nonEmpty)

  val arbiterLock = new LockUnits(this)
  arbiterLock.matcher = IsAll(Protoss.Arbiter, _.energy >= Protoss.Recall.energyCost || state == StateFighting)
  val armyLock = new LockUnits(this)
  armyLock.matcher = IsAny(Protoss.Zealot, Protoss.Dragoon, Protoss.HighTemplar, Protoss.Archon, Protoss.Reaver, Protoss.Carrier)
  armyLock.counter = CountBetween(10, 20)

  override protected def recruit(): Unit = {
    state = StateAssembling
  }

  trait MissionRecallState
  object StateAssembling extends MissionRecallState
  object StateEntering extends MissionRecallState
  object StateFighting extends MissionRecallState

  var state: MissionRecallState = StateAssembling

  override def run(): Unit = {
    // If arbiter is dead and we haven't recalled yet, abort
    // If arbiter lacks energy and we haven't recalled yet, abort
    // If arbiter is intercepted by an attacker far from arrival, abort
    // If all units are dead, abort

    // Find open area near army to gather
    // Find path for Arbiter and send it

    state match {
      case StateAssembling => assemble()
      case StateEntering => enter()
      case StateFighting => fight()
    }
  }

  private def assemble(): Unit = {
    // Gather army
    // Send Arbiter
  }
  private def enter(): Unit = {
    // Move to position and cast recall
    // If Arbiter loses energy without casting recall, abort
    // If Arbiter has cast recall, transition to fighting
  }
  private def fight(): Unit = {
    // Release any units which failed to teleport
    // Intend units to cantFlee
    // Target defenses -> key buildings -> workers -> anything else
  }
}

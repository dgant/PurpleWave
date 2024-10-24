package Tactic.Tactics

import Macro.Allocation.Prioritized
import Mathematics.Points.{Pixel, Points}
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.FriendlyUnitGroup

trait Tactic extends Prioritized with FriendlyUnitGroup {

  val lock: LockUnits = new LockUnits(this)
  var vicinity: Pixel = Points.middle

  @inline final def groupFriendlyUnits: Seq[FriendlyUnitInfo] = units

  def launch(): Unit
}

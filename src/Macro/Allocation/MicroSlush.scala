package Macro.Allocation

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass

class MicroSlush {
  private var _available = 0
  private var _spent = 0
  private var _lastAllocationFrame = Int.MinValue

  private def allocatedThisCycle: Boolean = _lastAllocationFrame >= With.priorities.lastResetFrame

  def available: Int = if (allocatedThisCycle) Math.max(0, _available - _spent) else 0

  def allocate(minerals: Int): Unit = {
    if ( ! allocatedThisCycle) {
      _available = 0
      _spent = 0
    }
    _available += minerals
    _lastAllocationFrame = With.frame
  }

  def trySpend(minerals: Int): Boolean = {
    val cost = Math.max(0, minerals)
    val output = cost <= available && cost <= With.self.minerals
    if (output) {
      _spent += cost
    }
    output
  }

  def trySpend(unitClass: UnitClass): Boolean = trySpend(unitClass.mineralPrice)
}

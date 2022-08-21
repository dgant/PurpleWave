package Tactic.Tactics.WorkerPulls

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait WorkerPull {
  def apply(): Int
  def minRemaining: Int = 0
  def employ(defenders: Seq[FriendlyUnitInfo]): Unit
}

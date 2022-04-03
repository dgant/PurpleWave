package Tactics.Production

import Macro.Buildables.RequestProduction
import Planning.Prioritized
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Production extends Prioritized {
  def isComplete: Boolean
  def hasSpent: Boolean
  def onUpdate(): Unit
  def onCompletion(): Unit = {}
  def expectUnit(unit: FriendlyUnitInfo): Boolean = false
  final def update(): Unit = {
    prioritize()
    onUpdate()
  }

  private var _buildable: RequestProduction = _
  final def buildable: RequestProduction = _buildable
  final def setBuildable(buildable: RequestProduction): RequestProduction = {
    _buildable = buildable
    buildable
  }

  final override def toString: String = f"Produce ${buildable.toString.replaceAll("Buildable ", "")}${if (isComplete) " (Complete)" else if (hasSpent) " (Spent)" else ""}"
}

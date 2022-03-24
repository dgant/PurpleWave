package Tactics.Production

import Macro.Buildables.Buildable
import Planning.Prioritized

trait Production extends Prioritized {
  def isComplete: Boolean
  def hasSpent: Boolean
  def onUpdate(): Unit
  def onCompletion(): Unit = {}
  final def update(): Unit = {
    prioritize()
    onUpdate()
  }

  private var _buildable: Buildable = _
  final def buildable: Buildable = _buildable
  final def setBuildable(buildable: Buildable): Buildable = {
    _buildable = buildable
    buildable
  }

  final override def toString: String = f"Produce ${buildable.toString.replaceAll("Buildable ", "")}${if (isComplete) " (Complete)" else if (hasSpent) " (Spent)" else ""}"
}

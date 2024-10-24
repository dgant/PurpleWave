package Tactic.Production

import Lifecycle.With
import Macro.Allocation.Prioritized
import Macro.Requests.RequestBuildable
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Production extends Prioritized {
  def isComplete: Boolean
  def hasSpent: Boolean
  def onUpdate(): Unit
  def expectTrainee(unit: FriendlyUnitInfo): Boolean = false
  def trainee: Option[FriendlyUnitInfo] = None
  final def update(): Unit = {
    if (With.blackboard.toCancel().contains(request.buildable)) return
    prioritize()
    onUpdate()
  }

  private var _request: RequestBuildable = _
  private var _expectedFrames: Int = _
  final def request: RequestBuildable = _request
  final def expectedFrames: Int = _expectedFrames
  final def setRequest(requestArg: RequestBuildable, expectedFramesArg: Int): Unit = {
    _request = requestArg
    _expectedFrames = expectedFramesArg
  }

  /**
    * Is the output of this production satisfactory for this request?
    */
  def satisfies(requirement: RequestBuildable): Boolean = {
    if (request.tech      != requirement.tech)      return false
    if (request.upgrade   != requirement.upgrade)   return false
    if (request.unit      != requirement.unit)      return false
    if (request.placement != requirement.placement) return false
    if ( ! request.specificTrainee.forall(trainee.contains)) return false
    if (request.upgrade.isDefined && request.quantity < requirement.quantity) return false
    if (request.minStartFrame > Math.max(requirement.minStartFrame, With.frame)) return false
    true
  }

  final override def toString: String = f"Produce ${request.toString.replaceAll("Buildable ", "")}${if (isComplete) " (Complete)" else if (hasSpent) " (Spent)" else ""}"
}

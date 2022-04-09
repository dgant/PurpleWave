package Tactic.Production

import Macro.Requests.RequestBuildable
import Planning.Prioritized
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait Production extends Prioritized {
  def isComplete: Boolean
  def hasSpent: Boolean
  def onUpdate(): Unit
  def onCompletion(): Unit = {}
  def expectTrainee(unit: FriendlyUnitInfo): Boolean = false
  def trainee: Option[FriendlyUnitInfo] = None
  final def update(): Unit = {
    prioritize()
    onUpdate()
  }

  private var _request: RequestBuildable = _
  final def request: RequestBuildable = _request
  final def setRequest(requestArg: RequestBuildable): RequestBuildable = {
    _request = requestArg
    _request
  }

  /**
    * Is the output of this production satisfactory for this request?
    */
  def satisfies(requirement: RequestBuildable): Boolean = {
    if (request.tech    != requirement.tech)    return false
    if (request.upgrade != requirement.upgrade) return false
    if (request.unit    != requirement.unit)    return false
    if (request.upgrade.isDefined && request.quantity < requirement.quantity) return false
    if ( ! request.specificUnit.forall(trainee.contains)) return false
    if (request.tileFilter != request.tileFilter) return false
    true
  }

  final override def toString: String = f"Produce ${request.toString.replaceAll("Buildable ", "")}${if (isComplete) " (Complete)" else if (hasSpent) " (Spent)" else ""}"
}
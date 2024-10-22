package Planning.Proxy

import Lifecycle.With
import Macro.Requests.RequestBuildable
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.Squad

class ProxyRequest(val request: RequestBuildable, val unit: Option[FriendlyUnitInfo], val squad: Option[Squad]) {
  val frameCreated: Int = With.frame
  def this(unit: FriendlyUnitInfo, requestBuildable: RequestBuildable) = {
    this(requestBuildable, Some(unit), None)
  }
  def this(squad: Squad, requestBuildable: RequestBuildable) = {
    this(requestBuildable, None, Some(squad))
  }
}

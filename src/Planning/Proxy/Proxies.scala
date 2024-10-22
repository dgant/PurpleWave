package Planning.Proxy

import Lifecycle.With
import Macro.Allocation.Prioritized
import Macro.Requests.RequestBuildable
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Squads.Squad

class Proxies extends Prioritized {

  var proxyRequests: Vector[ProxyRequest] = Vector.empty

  def request(unit: FriendlyUnitInfo, request: RequestBuildable): Unit = {
    proxyRequests :+= new ProxyRequest(unit, request)
  }

  def request(squad: Squad, request: RequestBuildable): Unit = {
    proxyRequests :+= new ProxyRequest(squad, request)
  }

  def update():  Unit = {
    val outdated = proxyRequests.view.filter(request => {
      val squad = request.squad
      val unit  = request.unit
      (
            (squad.isDefined && request.frameCreated < Math.min(With.squads.frameRun, With.tactics.lastRunFrame))
        ||  unit.map(_.agent.lastFrame).exists(_ > request.frameCreated)
        ||  (proxyRequests.exists(other => other.squad  .exists(squad.contains) && request.frameCreated < other.frameCreated))
        ||  (proxyRequests.exists(other => other.unit   .exists(unit.contains)  && request.frameCreated < other.frameCreated)))
    })
    proxyRequests = proxyRequests.filterNot(outdated.contains)
    proxyRequests.foreach(r => With.scheduler.request(this, r.request))
  }
}

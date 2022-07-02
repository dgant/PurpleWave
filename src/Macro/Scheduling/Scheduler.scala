package Macro.Scheduling

import Macro.Allocation.Prioritized
import Macro.Requests.RequestBuildable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Scheduler {
  private val _requests = new mutable.HashMap[Prioritized, mutable.ArrayBuffer[RequestBuildable]]

  def reset(): Unit = {
    _requests.clear()
  }

  def request(requester: Prioritized, theRequest: RequestBuildable): Unit = {
    requestAll(requester, Seq(theRequest))
  }

  def requestAll(requester: Prioritized, buildables: Iterable[RequestBuildable]): Unit = {
    if (buildables.forall(_.quantity <= 0)) return
    requester.prioritize()
    _requests(requester) = _requests.getOrElse(requester, ArrayBuffer.empty)
    _requests(requester) ++= buildables
  }

  def requests: Vector[(Prioritized, Iterable[RequestBuildable])] = {
    _requests.toVector.sortBy(_._1.priorityUntouched)
  }
}

package Macro.Scheduling

import Macro.Buildables.Buildable
import Planning.Prioritized

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Scheduler {
  private val _requests = new mutable.HashMap[Prioritized, mutable.ArrayBuffer[Buildable]]

  def reset() {
    _requests.clear()
  }

  def request(requester: Prioritized, theRequest: Buildable) {
    requestAll(requester, Seq(theRequest))
  }

  def requestAll(requester: Prioritized, buildables: Iterable[Buildable]): Unit = {
    requester.prioritize()
    _requests(requester) = _requests.getOrElse(requester, ArrayBuffer.empty)
    _requests(requester) ++= buildables
  }

  def requests: Vector[(Prioritized, Iterable[Buildable])] = {
    _requests.toVector.sortBy(_._1.priorityUntouched)
  }
}

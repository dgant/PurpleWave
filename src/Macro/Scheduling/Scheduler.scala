package Macro.Scheduling

import Macro.Requests.RequestBuildable

import scala.collection.mutable

class Scheduler {
  private val _requests = new mutable.ArrayBuffer[ScheduleItem]

  def reset(): Unit = {
    _requests.clear()
  }

  def request(requester: Any, theRequest: RequestBuildable): Unit = {
    if (theRequest.tech.isDefined || theRequest.quantity > 0) {
      _requests += ScheduleItem(requester, theRequest)
    }
  }

  def requests: Vector[ScheduleItem] = {
    _requests.toVector
  }
}

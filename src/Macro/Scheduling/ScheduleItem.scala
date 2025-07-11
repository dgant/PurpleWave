package Macro.Scheduling

import Macro.Requests.RequestBuildable

case class ScheduleItem(requester: Any, request: RequestBuildable) {
  override def toString = f"$request -- for $requester"
}

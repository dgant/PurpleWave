package Macro.Scheduling

import Macro.Requests.RequestBuildable

class MacroStep {
  val state: MacroState = new MacroState
  var event: MacroEvent = new MacroEvent(state)
  var request: Option[RequestBuildable] = None

  override def toString: String = f"${request.map(r => f"[$r] ").getOrElse("")}$event"
}

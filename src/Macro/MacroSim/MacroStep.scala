package Macro.MacroSim

import Macro.Requests.RequestProduction

class MacroStep {
  val state: MacroState = new MacroState
  var event: MacroEvent = new MacroEvent(state)
  var request: Option[RequestProduction] = None

  override def toString: String = f"${request.map(r => f"[$r] ").getOrElse("")}$event"
}

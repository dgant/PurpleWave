package Macro.MacroSim

import Macro.Buildables.Buildable

class MacroStep {
  val state: MacroState = new MacroState
  var event: MacroEvent = new MacroEvent(state)
  var request: Option[Buildable] = None

  override def toString: String = f"${event.toString}${request.map(" " + _.toString).getOrElse("")}"
}

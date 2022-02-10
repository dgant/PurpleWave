package Macro.MacroSim

class MacroStep {
  val state: MacroState = new MacroState
  var event: MacroEvent = new MacroEvent
  var request: Option[MacroRequest] = None

  override def toString: String = f"${event.toString} ${request.map(_.toString).getOrElse("")}"
}

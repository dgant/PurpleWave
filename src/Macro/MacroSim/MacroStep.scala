package Macro.MacroSim

class MacroStep {
  val state: MacroState = new MacroState
  var event: MacroEvent = new MacroEvent
  var request: Option[MacroRequest] = None
}

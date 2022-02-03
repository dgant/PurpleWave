package Macro.MacroSim

case class MacroStep(state: MacroState, event: MacroEvent, request: Option[MacroRequest] = None)

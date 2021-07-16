package Macro.MacroSim

import Lifecycle.With
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.Upgrades

final class MacroSim {
  var startFrame: Int = 0
  val states: Vector[MacroSimState] = (0 until 100).map(i => new MacroSimState).toVector
  val events: Vector[MacroSimEvent] = (0 until 100).map(i => new MacroSimEvent).toVector
  var iState: Int = 0
  var iEvent: Int = 0
  def nextState: MacroSimState = states(iState)
  def nextEvent: MacroSimEvent = events(iEvent)
  def dequeueState(): MacroSimState = { val n = nextState; iState += 1; n }
  def dequeueEvent(): MacroSimEvent = { val n = nextEvent; iEvent += 1; n }
  def reset(): Unit = {
    startFrame = With.frame
    var i = 0
    while (i < states.size && states(i).exists) { states(i).reset();  i += 1 }
    i = 0
    while (i < events.size && events(i).exists) { events(i).reset(); i += 1 }
    iState = 0
    iEvent = 0
    val state = dequeueState()
    state.minerals = With.self.minerals
    state.gas = With.self.gas
    state.supplyAvailable = With.units.ours.filter(_.complete).map(_.unitClass.supplyProvided).sum
    state.supplyUsed = With.units.ours.map(_.unitClass.supplyRequired).sum
    state.mineralPatches = With.self.bases.view.map(_.minerals.size).sum
    state.geysers = With.self.bases.view.map(_.gas.count(g => g.isOurs && g.gasLeft > 0)).sum
    state.techs.clear()
    state.techs ++= Techs.all.view.filter(With.self.hasTech)
    state.upgrades.clear()
    Upgrades.all.foreach(u => nextState.upgrades(u) = With.self.getUpgradeLevel(u))
    state.units.clear()
    With.units.ours.foreach(u => nextState.units(u.unitClass) += 1)
    // TODO: Add events for units which will become available in the future
  }
}

package Macro.Actions

import ProxyBwapi.Races.Protoss

object PumpShuttleAndReavers extends MacroActions {
  def apply(reavers: Int = 50, shuttleFirst: Boolean = true): Unit = {
    if (shuttleFirst && ! haveComplete(Protoss.RoboticsSupportBay)) {
      once(Protoss.Shuttle)
    }
    pumpRatio(Protoss.Shuttle, 0, reavers / 2, Seq(Friendly(Protoss.Reaver, 0.5)))
    pump(Protoss.Reaver, reavers)
    pumpRatio(Protoss.Shuttle, 0, (reavers + 1) / 2, Seq(Flat(0.5), Friendly(Protoss.Reaver, 0.5)), round = Rounding.Down)
  }
}

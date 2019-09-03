package Planning.Plans.Macro.Automatic

import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Predicates.Milestones.UnitsAtMost
import ProxyBwapi.Races.Protoss

class PumpShuttleAndReavers(reavers: Int = 50, shuttleFirst: Boolean = true) extends Parallel(
  (if (shuttleFirst)
    new If(
      new UnitsAtMost(0, Protoss.RoboticsSupportBay, complete = true),
      new Pump(Protoss.Shuttle, 1))
  else NoPlan()),

  new PumpRatio(Protoss.Shuttle, 0, reavers / 2, Seq(Friendly(Protoss.Reaver, 0.5))),
  new Pump(Protoss.Reaver, reavers),
  new PumpRatio(Protoss.Shuttle, 0, (reavers + 1) / 2, Seq(Flat(0.5), Friendly(Protoss.Reaver, 0.5))),
)

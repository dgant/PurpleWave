package Planning.Plans.Macro.Automatic

import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Predicates.Milestones.UnitsAtMost
import ProxyBwapi.Races.Protoss

class PumpShuttleAndReavers(reavers: Int = 50, shuttleFirst: Boolean = true) extends Parallel(
  (if (shuttleFirst)
    new If(
      new UnitsAtMost(0, Protoss.RoboticsFacility),
      new Pump(Protoss.Shuttle, 1))
  else NoPlan()
  ),
  new PumpMatchingRatio(Protoss.Shuttle, 0, 1, Seq(Friendly(Protoss.Reaver, 1.0))),
  new PumpMatchingRatio(Protoss.Shuttle, 0, reavers / 2, Seq(Friendly(Protoss.Reaver, 0.5))),
  new Pump(Protoss.Reaver, reavers)
)

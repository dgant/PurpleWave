package Planning.Plans.Macro.Automatic

import ProxyBwapi.Races.Protoss
import Startup.With

class TrainProbesContinuously extends TrainContinuously(Protoss.Probe) {
  
  override def maxDesirable: Int = Math.min(
    75,
    3 * With.geography.ourBases.toList.map(base => base.gas.size).sum +
    2 * With.geography.ourBases.toList.map(base => base.minerals.size).sum)
}

package Planning.Plans.Macro.Automatic

import Lifecycle.With
import ProxyBwapi.Races.Protoss

class TrainProbesContinuously extends TrainContinuously(Protoss.Probe) {
  
  override def maxDesirable: Int = Math.min(
    75,
    3 * With.geography.ourBases.toVector.map(base => base.gas.size).sum +
    2 * With.geography.ourBases.toVector.map(base => base.minerals.size).sum)
}

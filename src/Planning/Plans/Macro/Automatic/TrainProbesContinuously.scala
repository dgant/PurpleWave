package Planning.Plans.Macro.Automatic

import ProxyBwapi.Races.Protoss
import Startup.With

class TrainProbesContinuously extends TrainContinuously(Protoss.Probe) {
  
  override def maxDesirable: Int = Math.min(72, With.economy.ourMiningBases.size * 24)
}

package Planning.Plans.Macro.Automatic

import ProxyBwapi.Races.Protoss
import Startup.With

class TrainProbesContinuously extends TrainContinuously(Protoss.Probe) {
  
  override def maxDesirable: Int = With.economy.ourMiningBases.size * 24
}

package Plans.Macro.Automatic

import Startup.With

class BuildRefineryForEachBase extends TrainContinuously {
  
  description.set("Build bases when we're about saturated")
  
  unitType = With.game.self.getRace.getRefinery
  
  override def _totalRequired:Int = With.economy.ourMiningBases.size
}

package Plans.Macro.Automatic

import Startup.With

class BuildExpansionWhenSaturated extends TrainContinuously {
  
  description.set("Build bases when we're about saturated")
  
  unitType = With.self.getRace.getCenter
  
  override def _totalRequired:Int = 1 + With.units.ours.count(_.utype.isWorker) / 20
}

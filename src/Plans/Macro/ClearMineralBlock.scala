package Plans.Macro

import Plans.Plan
import Startup.With
import Types.UnitInfo.UnitInfo

class ClearMineralBlock extends Plan {
  
  override def onFrame() {
    if (isComplete) return
  }
  
  def getMinerals:Iterable[UnitInfo] = {
    With.units.neutral
  }
}

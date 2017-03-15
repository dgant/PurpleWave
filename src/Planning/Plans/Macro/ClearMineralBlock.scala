package Planning.Plans.Macro

import Planning.Plan
import Startup.With
import BWMirrorProxy.UnitInfo.UnitInfo

class ClearMineralBlock extends Plan {
  
  override def onFrame() {
    if (isComplete) return
  }
  
  def getMinerals:Iterable[UnitInfo] = {
    With.units.neutral
  }
}

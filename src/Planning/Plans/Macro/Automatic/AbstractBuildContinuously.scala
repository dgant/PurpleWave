package Planning.Plans.Macro.Automatic

import Planning.Plan
import Startup.With
import Macro.BuildRequests.RequestUnitAtLeast
import Performance.Caching.CacheFrame
import bwapi.UnitType

abstract class AbstractBuildContinuously extends Plan {
  
  protected def totalRequiredRecalculate:Int
  protected def unitType:UnitType
  
  override def isComplete:Boolean = totalRequired == 0
  override def onFrame() = With.scheduler.request(this, List(new RequestUnitAtLeast(totalRequired, unitType)))
  
  protected def totalRequired:Int = totalRequiredCache.get
  private val totalRequiredCache = new CacheFrame(() => totalRequiredRecalculate)
}

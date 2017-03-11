package Plans.Macro.Automatic

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import Utilities.Caching.Cache

abstract class AbstractBuildContinuously extends Plan {
  
  def _totalRequired:Int
  def _newBuild:Buildable
  
  override def isComplete:Boolean = totalRequired == 0
  override def onFrame() = With.scheduler.request(this, (0 until totalRequired).map(i => _newBuild))
  
  def totalRequired:Int = _totalRequiredCache.get
  val _totalRequiredCache = new Cache(1, () => _totalRequired)
}

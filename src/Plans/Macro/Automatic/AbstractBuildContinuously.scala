package Plans.Macro.Automatic

import Plans.Plan
import Startup.With
import Types.Buildable.Buildable
import Utilities.Caching.Cache

abstract class AbstractBuildContinuously extends Plan {
  
  def _buildsRequired:Int
  def _newBuild:Buildable
  
  override def isComplete:Boolean = buildsRequired == 0
  override def onFrame() = With.scheduler.request(this, (0 to buildsRequired).map(i => _newBuild))
  
  def buildsRequired:Int = _buildsRequiredCache.get
  val _buildsRequiredCache = new Cache(1, () => _buildsRequired)
}

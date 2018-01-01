package Performance

import Lifecycle.With
import Utilities.ByOption

class MicroReaction {
  
  def agencyLast      : Int = With.framesSince(With.agents.lastQueueCompletion)
  def battlesLast     : Int = With.framesSince(With.battles.clustering.lastClusterCompletion)
  def agencyMin       : Int = agencyMinCache()
  def agencyMax       : Int = agencyMaxCache()
  def battlesMax      : Int = battlesMaxCache()
  def agencyAverage   : Int = agencyAverageCache()
  def battlesAverage  : Int = battlesAverageCache()
  def framesTotal     : Int = agencyAverage + battlesAverage
  
  private val agencyMinCache      = new Cache(() => ByOption.min(With.agents.runtimes).getOrElse(0))
  private val agencyMaxCache      = new Cache(() => ByOption.max(With.agents.runtimes).getOrElse(0))
  private val battlesMaxCache     = new Cache(() => ByOption.max(With.battles.clustering.runtimes).getOrElse(0))
  private val agencyAverageCache  = new Cache(() => With.agents.runtimes.sum / Math.max(1, With.agents.runtimes.size))
  private val battlesAverageCache = new Cache(() => With.battles.clustering.runtimes.sum / Math.max(1, With.battles.clustering.runtimes.size))
}

package Performance

import Lifecycle.With
import Utilities.ByOption

class MicroReaction {
  
  def agencyLast          : Int = With.framesSince(With.agents.lastQueueCompletion)
  def clusteringLast      : Int = With.framesSince(With.battles.clustering.lastClusterCompletion)
  def estimationLast      : Int = With.framesSince(With.battles.lastEstimationCompletion)
  def agencyMin           : Int = agencyMinCache()
  def agencyMax           : Int = agencyMaxCache()
  def estimationMax       : Int = estimationMaxCache()
  def clusteringMax       : Int = clusteringMaxCache()
  def agencyAverage       : Int = agencyAverageCache()
  def estimationAverage   : Int = estimationAverageCache()
  def clusteringAverage   : Int = clusteringAverageCache()
  def framesTotal         : Int = agencyAverage + clusteringAverage
  
  private val agencyMinCache          = new Cache(() => ByOption.min(With.agents.runtimes).getOrElse(0))
  private val agencyMaxCache          = new Cache(() => ByOption.max(With.agents.runtimes).getOrElse(0))
  private val clusteringMaxCache      = new Cache(() => ByOption.max(With.battles.clustering.runtimes).getOrElse(0))
  private val estimationMaxCache      = new Cache(() => ByOption.max(With.battles.estimationRuntimes).getOrElse(0))
  private val agencyAverageCache      = new Cache(() => With.agents.runtimes.sum / Math.max(1, With.agents.runtimes.size))
  private val estimationAverageCache  = new Cache(() => With.battles.estimationRuntimes.sum / Math.max(1, With.battles.estimationRuntimes.size))
  private val clusteringAverageCache  = new Cache(() => With.battles.clustering.runtimes.sum / Math.max(1, With.battles.clustering.runtimes.size))
}

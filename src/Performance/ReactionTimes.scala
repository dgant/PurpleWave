package Performance

import Lifecycle.With
import Utilities.ByOption

class ReactionTimes {

  val runtimeQueueDuration: Int = 24 * 8
  
  def agencyLast          : Int = With.framesSince(With.agents.lastQueueCompletion)
  def clusteringLast      : Int = With.framesSince(With.battles.clustering.lastClusterCompletion)
  def estimationLast      : Int = With.framesSince(With.battles.lastEstimationCompletion)
  def planningLast        : Int = With.framesSince(With.prioritizer.lastRun)
  def agencyMin           : Int = agencyMinCache()
  def agencyMax           : Int = agencyMaxCache()
  def agencyAverage       : Int = agencyAverageCache()
  def estimationMax       : Int = estimationMaxCache()
  def estimationAverage   : Int = estimationAverageCache()
  def clusteringMax       : Int = clusteringMaxCache()
  def clusteringAverage   : Int = clusteringAverageCache()
  def planningMax         : Int = planningMaxCache()
  def planningAverage     : Int = planningAverageCache()
  def framesTotal         : Int = agencyAverage + estimationAverage + clusteringAverage

  def sluggishness = _sluggishness()
  private val sluggishThresholds = Seq(4, 8, 12)
  private val _sluggishness = new Cache(() => sluggishThresholds.zipWithIndex.find(agencyAverage < _._1).map(_._2).getOrElse(sluggishThresholds.size))

  def filterTimes(times: Seq[Int]): Seq[Int] = times // Optional: Remove outliers
  
  private val agencyMinCache            = new Cache(() => ByOption.min(With.agents.cycleLengths).getOrElse(0))
  private val agencyMaxCache            = new Cache(() => ByOption.max(With.agents.cycleLengths).getOrElse(0))
  private val clusteringMaxCache        = new Cache(() => ByOption.max(With.battles.clustering.runtimes).getOrElse(0))
  private val estimationMaxCache        = new Cache(() => ByOption.max(With.battles.estimationRuntimes).getOrElse(0))
  private val planningMaxCache          = new Cache(() => ByOption.max(With.prioritizer.frameDelays).getOrElse(0))
  private val agencyAverageCache        = new Cache(() => filterTimes(With.agents.cycleLengths).sum             / Math.max(1, filterTimes(With.agents.cycleLengths).size))
  private val estimationAverageCache    = new Cache(() => filterTimes(With.battles.estimationRuntimes).sum  / Math.max(1, filterTimes(With.battles.estimationRuntimes).size))
  private val clusteringAverageCache    = new Cache(() => filterTimes(With.battles.clustering.runtimes).sum / Math.max(1, filterTimes(With.battles.clustering.runtimes).size))
  private val planningAverageCache      = new Cache(() => filterTimes(With.prioritizer.frameDelays).sum     / Math.max(1, filterTimes(With.prioritizer.frameDelays).size))
}

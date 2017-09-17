package Performance

import Lifecycle.With
import Utilities.ByOption

object MicroReaction {
  
  def agencyLast      : Int = With.framesSince(With.agents.lastQueueCompletion)
  def battlesLast     : Int = With.framesSince(With.battles.clustering.lastClusterCompletion)
  def agencyMax       : Int = ByOption.max(With.agents.runtimes).getOrElse(0)
  def battlesMax      : Int = ByOption.max(With.battles.clustering.runtimes).getOrElse(0)
  def agencyAverage   : Int = With.agents.runtimes.sum / Math.max(1, With.agents.runtimes.size)
  def battlesAverage  : Int = With.battles.clustering.runtimes.sum / Math.max(1, With.battles.clustering.runtimes.size)
  def framesTotal     : Int = agencyAverage + battlesAverage
}

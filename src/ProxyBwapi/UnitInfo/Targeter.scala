package ProxyBwapi.UnitInfo

import Lifecycle.With
import Micro.Targeting.TargetScoring
import Performance.Cache

trait Targeter {
  def targetScore(target: UnitInfo): Double = targetScores(target.id)()
  val targetScores: Array[Cache[Double]] = (0 until 10000).map(i => new Cache[Double](() =>
    With.units.getId(i)
      .map(target => TargetScoring.score(this.asInstanceOf[FriendlyUnitInfo], target))
      .getOrElse(0.0))).toArray
}

package ProxyBwapi.UnitInfo

import Lifecycle.With
import Micro.Targeting.TargetScoring
import Performance.Cache

import scala.collection.mutable

trait Targeter {

  def targetScore(target: UnitInfo): Double = {
    targetScores.getOrElseUpdate(
      target.id,
      new Cache[Double](() =>
        With.units.getId(target.id)
          .map(target => TargetScoring.slow(this.asInstanceOf[FriendlyUnitInfo], target))
          .getOrElse(0.0)))()
  }

  val targetScores = new mutable.HashMap[Int, Cache[Double]]()
}
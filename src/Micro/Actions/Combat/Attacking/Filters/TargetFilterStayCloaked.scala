package Micro.Actions.Combat.Attacking.Filters
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterStayCloaked extends TargetFilter {
  
  // Preserve cloaked units.
  // Don't compromise their detection.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cloaked = actor.effectivelyCloaked
    lazy val reveals = With.grids.enemyDetection.isSet(target.tileIncludingCenter)
    val output = ! cloaked || ! reveals
    output
  }
  
}

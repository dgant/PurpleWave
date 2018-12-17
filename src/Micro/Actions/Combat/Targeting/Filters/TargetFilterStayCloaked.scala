package Micro.Actions.Combat.Targeting.Filters
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterStayCloaked extends TargetFilter {
  
  // Preserve cloaked units.
  // Don't compromise their detection.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cloaked  = actor.effectivelyCloaked
    lazy val range    = actor.pixelRangeAgainst(target)
    lazy val distance = actor.pixelDistanceEdge(target)
    lazy val pixel    = actor.pixelToFireAt(target)
    lazy val reveals  = With.grids.enemyDetection.isDetected(pixel.tileIncluding)

    val output = ! cloaked || ! reveals
    output
  }
  
}

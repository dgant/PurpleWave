package Micro.Actions.Combat.Targeting.Filters
import Lifecycle.With
import Mathematics.Points.TileRectangle
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterStayCloaked extends TargetFilter {
  
  // Preserve cloaked units.
  // Don't compromise their detection.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cloaked  = actor.effectivelyCloaked
    lazy val pixel    = actor.pixelToFireAt(target)
    lazy val reveals  = new TileRectangle(pixel.tileIncluding).expand(2, 2).tiles.exists(With.grids.enemyDetection.isDetected)

    val output = ! cloaked || ! reveals
    output
  }
  
}

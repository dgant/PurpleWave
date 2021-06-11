package Micro.Targeting.Filters
import Mathematics.Points.TileRectangle
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCloaked extends TargetFilter {
  simulationSafe = true
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.cloaked && ! actor.matchups.arbiterCovering()
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cloaked  = actor.effectivelyCloaked
    lazy val pixel    = actor.pixelToFireAt(target)
    lazy val reveals  = ! actor.inRangeToAttack(target) && new TileRectangle(pixel.tile).expand(2, 2).tiles.exists(_.enemyDetected)
    lazy val detecting = actor.inRangeToAttack(target) && target.unitClass.isDetector
    ! cloaked || ! reveals || detecting
  }
  
}
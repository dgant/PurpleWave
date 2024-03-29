package Micro.Targeting.FiltersRequired

import Mathematics.Points.TileRectangle
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterStayCloaked extends TargetFilter {
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = actor.cloaked && ! actor.matchups.groupOf.arbiters.exists(_.pixelDistanceEdge(actor) < 160)
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cloaked    = actor.effectivelyCloaked
    lazy val detecting  = actor.inRangeToAttack(target) && target.unitClass.isDetector
    lazy val pixel      = actor.pixelToFireAtExhaustive(target)
    lazy val reveals    = ! actor.inRangeToAttack(target) && new TileRectangle(pixel.tile).expand(2, 2).tiles.exists(_.enemyDetected)

    ! cloaked || detecting || ! reveals
  }
  
}

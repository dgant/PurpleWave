package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Mathematics.Points.TileRectangle
import Micro.Targeting.TargetFilter
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterStayCloaked extends TargetFilter {

  override def appliesTo(actor: FriendlyUnitInfo): Boolean = (
    actor.effectivelyCloaked
    && actor.isAny(Terran.Wraith, Protoss.DarkTemplar)
    && ! With.yolo.active)

  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val detecting  = actor.inRangeToAttack(target) && target.unitClass.isDetector
    lazy val pixel      = actor.pixelToFireAtExhaustive(target)
    lazy val reveals    = ! actor.inRangeToAttack(target) && new TileRectangle(pixel.tile).expand(2, 2).tiles.exists(_.enemyDetected)

    detecting || ! reveals
  }
}

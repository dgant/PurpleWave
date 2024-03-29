package Micro.Coordination.Pushing

import Mathematics.Maff
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}


object SpiderMineMath {
  val radius = 100
  def expectedTarget(mine: UnitInfo): Option[UnitInfo] = {
    val targetPosition = mine.orderTargetPixel.getOrElse(mine.pixel)
    val target = Maff.minBy(mine.matchups.targets)(_.pixelDistanceEdge(targetPosition))
    if (target.forall(_.pixelDistanceEdge(targetPosition) > radius))
      None
    else
      target
  }
  def expectedPositionGivenTarget(mine: UnitInfo, target: Option[UnitInfo]): Pixel = {
    target
      .map(t => t.pixel.project(mine.pixel, t.unitClass.dimensionMax + Terran.SpiderMine.dimensionMax))
      .orElse(mine.targetPixel)
      .orElse(mine.orderTargetPixel)
      .getOrElse(mine.pixel)
  }
  def expectedPosition(mine: UnitInfo): Pixel = {
    expectedPositionGivenTarget(mine, expectedTarget(mine))
  }
}

class ExplosionSpiderMineBlast(mine: UnitInfo) extends CircularPush(TrafficPriorities.Dodge, SpiderMineMath.expectedPosition(mine), SpiderMineMath.radius ) {
  val expectedTarget: Option[UnitInfo] = SpiderMineMath.expectedTarget(mine)
  val expectedTargetPosition: Pixel = SpiderMineMath.expectedPositionGivenTarget(mine, expectedTarget)

  override def force(recipient: FriendlyUnitInfo): Option[Force] = {
    if (recipient.flying || recipient.unitClass.isBuilding) {
      None
    } else if(
      // We can defuse the mine
      recipient.canAttack(mine)
      && recipient.readyForAttackOrder
      && recipient.pixelRangeAgainst(mine) > SpiderMineMath.radius) {
      None
    } else if(
      // TODO: We are mine dragging
      false
    ) {
      None
    } else {
      super.force(recipient)
    }
  }
}

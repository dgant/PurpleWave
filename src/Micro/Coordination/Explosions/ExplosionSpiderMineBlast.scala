package Micro.Coordination.Explosions

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class ExplosionSpiderMineBlast(mine: UnitInfo) extends ExplosionRadial {
  
  protected lazy val expectedTarget: Option[UnitInfo] = {
    val targetPosition = mine.orderTargetPixel.getOrElse(mine.pixelCenter)
    val target = ByOption.minBy(mine.matchups.targets)(_.pixelDistanceEdge(targetPosition))
    if (target.forall(_.pixelDistanceEdge(targetPosition) > radius))
      None
    else
      target
  }
  
  override def center: Pixel = {
    expectedTarget
      .map(target =>
        mine.pixelCenter.project(
          target.pixelCenter,
          mine.pixelDistanceEdge(target) - mine.pixelRangeGround))
      .getOrElse(mine.pixelCenter)
  }
  override def radius: Double = 100
  
  override def affects(unit: UnitInfo): Boolean = (
    ! unit.flying
    && ! unit.unitClass.isBuilding
    && ! (
    center == mine.pixelCenter
      && unit.canAttack(mine)
      && unit.readyForAttackOrder
      && unit.pixelRangeAgainst(mine) > radius)
    && expectedTarget.isDefined
    && ! (
      expectedTarget.contains(unit)
      && unit.matchups.targets.exists(target =>
        target.subjectiveValue > unit.subjectiveValue
        && mine.canAttack(target)
        && mine.pixelDistanceEdge(target) < mine.pixelDistanceEdge(unit)))
    
  )
  
  override def framesRemaining: Double = GameTime(0, 1)()
}

package Micro.Coordination.Explosions

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class ExplosionSpiderMineBlast(mine: UnitInfo) extends ExplosionRadial {
  protected val expectedTarget: Option[UnitInfo] = {
    val targets = mine.matchups.targets
    val target = ByOption.minBy(targets)(target => {
      val distanceNow = target.pixelDistanceEdge(mine)
      val distanceLater = target.pixelDistanceEdge(mine, mine.projectFrames(12))
      distanceNow + 2 * distanceLater
    })
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
  
  override def affects(unit: UnitInfo): Boolean = expectedTarget.isDefined && ! unit.flying && ! unit.unitClass.isBuilding
  
  override def framesRemaining: Double = GameTime(0, 1)()
}

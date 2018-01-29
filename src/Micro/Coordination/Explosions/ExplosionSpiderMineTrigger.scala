package Micro.Coordination.Explosions

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

class ExplosionSpiderMineTrigger(mine: UnitInfo) extends ExplosionRadial {
  // Spider Mines rely on normal target acquisition range math, which is 96px + attack range edge-to-edge
  // However, this is often limited by Spider Mine sight range, which is 96px exactly.
  // According to jaj22 the acqusition range is center-to-center (unlike most units, which are edge-to-edge)
  override def center: Pixel = mine.pixelCenter
  override def radius: Double = 96 + mine.pixelRangeMax
  override def affects(unit: UnitInfo): Boolean = (
    (mine.burrowed || (
      With.framesSince(mine.frameDiscovered) < 48)
      && mine.isEnemy || mine.matchups.targets.exists(_.pixelDistanceCenter(mine) <= radius))
    && ! unit.flying
    && ! unit.unitClass.floats
    && ! unit.unitClass.isBuilding
    && unit.isEnemyOf(mine)
  )
  override def framesRemaining: Double = Double.PositiveInfinity
}

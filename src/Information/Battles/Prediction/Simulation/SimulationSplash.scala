package Information.Battles.Prediction.Simulation

import Mathematics.Maff
import ProxyBwapi.Size
import bwapi.{DamageType, WeaponType}

private[Simulation] object SimulationSplash {
  def healthMultiplier(weapon: WeaponType, size: Size.Type): Double = {
    if (weapon.damageType == DamageType.Explosive) {
      if (size == Size.Small) 0.5 else if (size == Size.Medium) 0.75 else 1.0
    } else if (weapon.damageType == DamageType.Concussive) {
      if (size == Size.Large) 0.25 else if (size == Size.Medium) 0.5 else 1.0
    } else 1.0
  }

  def pointDistance(unit: Simulacrum, x: Int, y: Int): Double = Maff.broodWarDistance(0, 0,
    Math.max(0, Math.max(unit.pixelLeft - x, x - (unit.pixelRightExclusive - 1))),
    Math.max(0, Math.max(unit.pixelTop - y, y - (unit.pixelBottomExclusive - 1))))

  // Approximate the traveling Lurker spines as one continuous segment, hitting each footprint once.
  def lineDistance(unit: Simulacrum, ax: Double, ay: Double, bx: Double, by: Double): Double = {
    val left = unit.pixelLeft.toDouble
    val right = unit.pixelRightExclusive - 1.0
    val top = unit.pixelTop.toDouble
    val bottom = unit.pixelBottomExclusive - 1.0
    val dx = bx - ax
    val dy = by - ay
    val lengthSquared = dx * dx + dy * dy
    def pointToBox(x: Double, y: Double): Double = Math.hypot(
      Math.max(0, Math.max(left - x, x - right)), Math.max(0, Math.max(top - y, y - bottom)))
    if (lengthSquared == 0) return pointToBox(ax, ay)
    val tx1 = if (dx == 0) Double.NegativeInfinity else (left - ax) / dx
    val tx2 = if (dx == 0) Double.PositiveInfinity else (right - ax) / dx
    val ty1 = if (dy == 0) Double.NegativeInfinity else (top - ay) / dy
    val ty2 = if (dy == 0) Double.PositiveInfinity else (bottom - ay) / dy
    val entry = Math.max(0, Math.max(Math.min(tx1, tx2), Math.min(ty1, ty2)))
    val exit = Math.min(1, Math.min(Math.max(tx1, tx2), Math.max(ty1, ty2)))
    if (entry <= exit && (dx != 0 || (ax >= left && ax <= right)) && (dy != 0 || (ay >= top && ay <= bottom))) return 0
    def cornerDistance(x: Double, y: Double): Double = {
      val t = Maff.clamp(((x - ax) * dx + (y - ay) * dy) / lengthSquared, 0.0, 1.0)
      Math.hypot(x - ax - t * dx, y - ay - t * dy)
    }
    Math.min(Math.min(pointToBox(ax, ay), pointToBox(bx, by)),
      Math.min(Math.min(cornerDistance(left, top), cornerDistance(right, top)),
        Math.min(cornerDistance(left, bottom), cornerDistance(right, bottom))))
  }
}

package Planning.UnitPreferences

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class PreferCloseAndNotMining(pixel: Pixel = SpecificPoints.middle) extends Preference {
  
  override def apply(unit: FriendlyUnitInfo): Double = {
    val busyCarrying  = unit.carrying
    val busyGassing   = unit.target.exists(t => t.unitClass.isGas)
    val busyDrilling  = unit.target.exists(t => t.unitClass.isMinerals && ! unit.moving && unit.pixelDistanceCenter(t) < 72.0)
    val baseCost      = unit.framesToTravelTo(pixel)
    val multiplier    = if (busyCarrying || busyGassing || busyDrilling) 1.3 else 1.0
    baseCost * multiplier
  }
}

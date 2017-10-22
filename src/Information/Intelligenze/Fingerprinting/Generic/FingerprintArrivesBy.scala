package Information.Intelligenze.Fingerprinting.Generic

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class FingerprintArrivesBy(
  unitClass : UnitClass,
  gameTime  : GameTime,
  quantity  : Int = 1)
    extends Fingerprint {
  
  trigger = true
  
  override def investigate: Boolean = {
    // NOTE: THIS IS BUGGED because units can have completionTime Int.MAX which then overflows
    val units           = With.units.enemy.filter(_.is(unitClass))
    val arrivalFrame    = gameTime.frames
    val arrivalTimes    = units.map(u => (u, arrivaltime(u))).toMap
    val arrivingOnTime  = arrivalTimes.count(_._2 < arrivalFrame)
    val output          = arrivingOnTime >= quantity
    output
  }
  
  protected def arrivaltime(unit: UnitInfo): Int = {
    val home        = With.geography.home.pixelCenter
    val classSpeed  = unit.unitClass.topSpeed
    val travelTime  = Math.min(24 * 60 * 60,
      if (unit.canMove)
        unit.framesToTravelTo(home)
      else if (classSpeed > 0)
        (unit.pixelDistanceTravelling(home) / classSpeed).toInt
      else
        Int.MaxValue)
    
    val completionTime  = Math.max(With.frame, unit.completionFrame)
    val arrivalTime     = completionTime + travelTime
    arrivalTime
  }
}

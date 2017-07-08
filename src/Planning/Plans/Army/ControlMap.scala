package Planning.Plans.Army

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Planning.Plan

import scala.collection.mutable

class ControlMap extends Plan {
  
  private val zones = new mutable.HashMap[Zone, Plan]
  
  protected override def onUpdate() {
    initialize()
    zones.keys.toList
      .sortBy(-zoneValue(_))
      .foreach(zones(_).update())
  }
  
  private def initialize() {
    if (zones.nonEmpty) return
    With.geography.zones.foreach(zone => zones.put(zone, new ControlZone(zone)))
  }
  
  private def zoneValue(zone: Zone): Double = {
    zone.bases.map(baseValue).sum
  }
  
  private def baseValue(base: Base): Double = {
    (5.0 + base.workers.size) *
      (
        if (base.owner.isFriendly)
          100.0
        else if (base.planningToTake)
          50.0
        else if (base.owner.isEnemy)
          20.0
        else
          1.0
      )
  }
}

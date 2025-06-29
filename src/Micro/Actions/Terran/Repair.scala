package Micro.Actions.Terran

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Repair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = Terran.SCV(unit) && unit.intent.toHeal.exists(t => t.hitPoints < t.unitClass.maxTotalHealth)
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.loaded) {
      unit.transport.foreach(Commander.unload(_, unit))
      return
    }

    unit.intent.toHeal.foreach(patient =>
      if (patient.hitPoints < patient.unitClass.maxHitPoints) {
        Commander.repair(unit, patient)
      } else {
        unit.agent.decision.set(patient.pixel.project(unit.pixel, 48))
        Commander.move(unit)
      })
  }
}

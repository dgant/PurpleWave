package Micro.Coordination

import Lifecycle.With
import ProxyBwapi.Orders
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Healing {
  var lastHealing: mutable.HashMap[UnitInfo, UnitInfo] = mutable.HashMap.empty
  var nextHealing: mutable.HashMap[UnitInfo, UnitInfo] = mutable.HashMap.empty

  def toHeal(healer: UnitInfo): Option[UnitInfo] = {
    nextHealing.get(healer).orElse(lastHealing.get(healer))
  }

  def healer(patient: UnitInfo): Option[UnitInfo] = {
    nextHealing.find(_._2 == patient).orElse(lastHealing.find(_._2 == patient)).map(_._1)
  }

  def heal(healer: UnitInfo, patient: UnitInfo): Unit = {
    healer.presumedHealing = Some(patient)
    patient.healers.include(healer)
    healer.friendly.foreach(_.intent.setHeal(patient))
    nextHealing(healer) = patient
  }

  def update(): Unit = {
    if ( ! With.self.isTerran) return

    val lastLastHealing = lastHealing
    lastHealing = nextHealing
    nextHealing = lastLastHealing
    nextHealing.clear()

    With.units.playerOwned.foreach(u => {
      u.healers.clear()
      u.presumedHealing = None
    })
    With.units.playerOwned.foreach(healer => if (healer.aliveAndComplete) {
      var healing: Boolean = false
      healing ||= Terran.SCV(healer)    && (healer.order == Orders.Repair || healer.order == Orders.MoveToRepair)
      healing ||= Terran.Medic(healer)  && (healer.order == Orders.HealMove || healer.order == Orders.MedicHeal || healer.order == Orders.MedicHealToIdle)
      if (healing) {
        val organicTarget = Terran.Medic(healer)
        val patient = healer.orderTarget.filter(patient => patient.player == healer.player && patient.hitPoints < patient.unitClass.maxTotalHealth)
        patient.flatMap(_.friendly).foreach(p => healer.friendly.foreach(h => heal(h, p)))
      }
    })

    (lastHealing ++ nextHealing).foreach(p => p._1.friendly.foreach(_.intent.setHeal(p._2)))
  }

}

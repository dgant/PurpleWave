package Information.Scouting

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.CountMap
import Utilities.Time.Forever

trait Timings {
  private val enemyContacts = new CountMap[UnitClass](Forever())

  protected def updateTimings(): Unit = {
    val ourContactPoints =
      With.geography.ourBases.flatMap(b =>
        b.zone.exitNow.map(_.pixelCenter).toSeq
        ++ Seq(b.heart.center))
    if (ourContactPoints.nonEmpty) {
      With.units.enemy
        .filter(_.visible)
        .filter(u => enemyContacts(u.unitClass) > With.frame)
        .foreach(u =>enemyContacts.reduceTo(u.unitClass,  ourContactPoints.view.map(u.framesToTravelTo).min))
    }
  }

  def enemyContact(unitClass: UnitClass): Int = enemyContacts(unitClass)
}

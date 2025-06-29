package Micro.Actions.Terran

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object GetRepaired extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.self.isTerran
    && unit.unitClass.isMechanical
    && ! unit.sieged
    && ! unit.agent.commit
    && ! unit.loaded
    && ! With.yolo.active
    && unit.loadedUnits.isEmpty
    && {
      var interested = unit.hitPoints < unit.unitClass.maxHitPoints
      interested &&= unit.healers.nonEmpty || (unit.base.exists(_.isOurs) && unit.matchups.pixelsToThreatRange.exists(_ < 256))
      interested ||= unit.hitPoints <= unit.unitClass.repairThreshold
      interested
    })

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return

    var repairGoal: Option[Pixel] = None
    var repairUnit: Option[FriendlyUnitInfo] = None

    lazy val squire     = Maff.minBy(unit.healers)(h => unit.pixelDistanceTravelling(h.pixel))
    lazy val fieldMedic = Maff.minBy(unit.squad.view.flatMap(_.units.view.filter(u => Terran.SCV(u) && u.intent.toHeal.forall(h => h == unit || (h.hitPoints >= h.unitClass.maxHitPoints && h.healers.length > 1)))))(scv => unit.pixelDistanceTravelling(scv.pixel))
    lazy val hospital   = Maff.minBy(With.geography.ourBases.filter(_.ourUnits.exists(Terran.SCV)))(b => unit.pixelDistanceTravelling(b.heart))

    if (squire.isDefined) {
      repairGoal = squire.map(_.pixel)
      repairUnit = squire.flatMap(_.friendly)
    } else if (fieldMedic.isDefined) {
      repairGoal = fieldMedic.map(_.pixel)
      repairUnit = fieldMedic
    } else if (hospital.isDefined) {
      repairGoal = hospital.map(b => {
        val from = b.townHallArea.center
        b.exitNow
          .map(_.pixelCenter)
          .map(exit => from.project(exit, Math.min(from.pixelDistance(exit) - 32 * 6, 32 * 8)).walkablePixel)
          .getOrElse(b.heart.center)
        })
      repairGoal.foreach(goal => {
        if (unit.pixelDistanceCenter(goal) < 128) {
          repairUnit = Maff.minBy(hospital.get.ourUnits.view
            .flatMap(_.friendly)
            .filter(u => u.intent.toGather.isDefined && u.intent.toHeal.forall(unit==)))(scv => unit.pixelDistanceTravelling(unit.pixel))
        }
      })
    }

    repairGoal.foreach(unit.agent.station.set)
    repairUnit.foreach(With.coordinator.healing.heal(_, unit))

    if (repairGoal.isDefined) {
      unit.intent.setCanFight(false)
      unit.agent.shouldFight = false
      unit.agent.fightReason = "GetRepaired"
    }

    squire.filter(_.inRangeToAttack(unit)).foreach(s => {
      if (Terran.SCV(unit) && s.hitPoints < s.unitClass.maxTotalHealth) {
        Commander.repair(unit, s)
      } else {
        Potshot.delegate(unit)
        if (unit.ready) {
          Commander.hold(unit)
        }
      }
    })
  }
}

package Tactic.Tactics

import Lifecycle.With
import Macro.Actions.MacroActions
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.Time.{Forever, Minutes, Seconds}
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsWorker, UnitFilter}
import Utilities.UnitPreferences.PreferClose

class TacticRalph extends Tactic with MacroActions {

  var lastRalph: Option[FriendlyUnitInfo] = None
  var lastDeath: Int = - Forever()

  override def launch(): Unit = {
    if (lastRalph.exists( ! _.alive)) {
      lastDeath = With.frame
      lastRalph = None
    }

    val workerScout = With.tactics.workerScout
    val attackSquad = With.tactics.attackSquad

    if (With.framesSince(lastDeath) < Seconds(10)())                                                return
    if (With.scouting.ourProximity < 0.5)                                                           return
    if (With.self.supplyUsed200 < 30)                                                               return
    if (workerScout.units.nonEmpty)                                                                 return
    if ( ! workerScout.scoutingAbandoned && With.frame < Minutes(4)())                              return
    if (attackSquad.units.exists(_.proximity < 0.3))                                                return
    if (attackSquad.units.nonEmpty && With.tactics.attackSquad.centroidKey.metro.exists(_.isEnemy)) return

    val ralphClass: UnitFilter =
      ?(haveComplete(Terran.Vulture),           Terran.Vulture,
      ?(haveComplete(Protoss.Observer),         Protoss.Observer,
      ?(haveComplete(Zerg.Queen),               Zerg.Queen,
      ?(upgradeComplete(Zerg.OverlordSpeed),    Zerg.Overlord,
      ?(haveComplete(Zerg.Scourge),             Zerg.Scourge,
      ?(haveComplete(Zerg.Zergling),            Zerg.Zergling,
      ?(upgradeComplete(Protoss.ZealotSpeed),   Protoss.Zealot,
      ?(haveComplete(Protoss.Observer),         Protoss.Observer,
      IsWorker))))))))

    vicinity = With.scouting.enemyThreatOrigin.walkableTile.center
    if (vicinity.metro.exists(_.isEnemy)) {
      vicinity = vicinity.metro.get.natural.map(nat =>
        Maff.centroid(
          Seq(
            nat.entranceOriginal.map(_.pixelCenter),
            nat.exitOriginal.map(_.pixelCenter),
            Some(nat.townHallArea.center),
            Some(nat.zone.downtown.center))
          .flatten)
          .walkablePixel
      ).getOrElse(vicinity)
    }

    lock
      .setCounter(CountOne)
      .setMatcher(ralphClass)
      .setPreference(PreferClose(vicinity))

    if (ralphClass == IsWorker
      && With.units.countOurs(IsWorker) < 22
      && lock.inquire().forall(_.forall(_.proximity > 0.5))) {
      return
    }

    lock.acquire()
    lastRalph = lock.units.headOption
    if (lastRalph.isDefined) {
      status("Ralph")
    }
    lock.units.foreach(ralph =>
      ralph.intend(this)
        .setTerminus(vicinity)
        .setCanFight(ralph.canAttackGround && ralph.matchups.threatDeepest.forall(IsWorker))
        .setCanSneak(true)
    )
  }
}

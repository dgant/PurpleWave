package Tactic.Tactics

import Lifecycle.With
import Macro.Facts.MacroCounting
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.Time.{Forever, Minutes, Seconds}
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.{IsWorker, UnitFilter}
import Utilities.UnitPreferences.PreferClose

class TacticRalph extends Tactic with MacroCounting {

  var lastRalph: Option[FriendlyUnitInfo] = None
  var lastDeath: Int = - Forever()

  override def launch(): Unit = {
    if (lastRalph.exists( ! _.alive)) {
      lastDeath = With.frame
    }
    lastRalph = lastRalph.filter(_.client != this)

    if (With.tactics.workerScout.units.nonEmpty) return
    if ( ! With.tactics.workerScout.scoutingAbandoned && With.frame < Minutes(4)()) return
    if (With.framesSince(lastDeath) < Seconds(20)()) return
    if (With.scouting.ourProximity < 0.2) return

    val ralphClass: UnitFilter =
      ?(haveComplete(Terran.Vulture),           Terran.Vulture,
      ?(haveComplete(Protoss.Observer),         Protoss.Observer,
      ?(haveComplete(Zerg.Queen),               Zerg.Queen,
      ?(upgradeComplete(Zerg.OverlordSpeed),    Zerg.Overlord,
      ?(haveComplete(Zerg.Scourge),             Zerg.Scourge,
      ?(haveComplete(Zerg.Zergling),            Zerg.Zergling,
      ?(upgradeComplete(Protoss.ZealotSpeed),   Protoss.Zealot,
      IsWorker)))))))

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

    lock.acquire()
    lastRalph = lock.units.headOption
    lock.units.foreach(ralph =>
      ralph.intend(this)
        .setTerminus(vicinity)
        .setCanFight(ralph.canAttackGround && ralph.matchups.threatDeepest.forall(IsWorker))
        .setCanSneak(true)
    )
  }
}

package Tactic.Tactics

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Strategery.Strategies.Zerg.ZvE4Pool
import Utilities.?
import Utilities.Time.Seconds
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAny, IsComplete, IsSpeedling, IsTank, IsWorker}
import Utilities.UnitPreferences.PreferScout

class TacticWorkerScout extends Tactic {

  var scoutingAbandoned : Boolean                   = false
  var scouts            : Vector[FriendlyUnitInfo]  = Vector.empty

  lock.setMatcher(IsWorker).setInterruptible(false)

  def launch(): Unit = {
    if (scoutingAbandoned) return

    val basesToScout = Maff.orElseFiltered(
      ?(With.scouting.enemyMainFullyScouted,
        With.geography.enemyBases.flatMap(b => Vector(b) ++ b.natural ++ b.naturalOf).distinct,
        With.scouting.enemyMain.toVector),
      With.geography.mains.filterNot(_.scoutedByUs),
      With.geography.neutralBases)( ! _.island).toVector

    scoutingAbandoned ||= scouts.exists( ! _.alive)
    scoutingAbandoned ||= basesToScout.isEmpty

    if ( ! ZvE4Pool()) {
      // Abandon scouting when we have a closer unit approaching
      scoutingAbandoned ||= (
        With.blackboard.wantToAttack()
        && scouts.nonEmpty
        && vicinity.metro.exists(m => ! scouts.exists(_.metro.contains(m)))
        && With.units.ours.exists(u =>
          u.complete
          && ! IsWorker(u)
          && With.geography.enemyBases.view.map(_.heart).exists(h => scouts.forall(_.framesToTravelTo(h) > u.framesToTravelTo(h) - Seconds(5)()))))

      // Abandon scouting when they can catch our scout (and we're not counting on the scout to help fight)
      scoutingAbandoned ||= With.units.enemy.exists(_.isAll(
        IsComplete,
        IsAny(
          Terran.Marine,
          Terran.Vulture,
          Terran.Factory,
          IsTank,
          Protoss.Dragoon,
          IsSpeedling,
          Zerg.Hydralisk,
          Zerg.Mutalisk,
          Zerg.Spire)))
      scoutingAbandoned ||= With.units.enemy.exists(e => e.isAll(IsComplete, Protoss.PhotonCannon, Zerg.SunkenColony) && scouts.forall(_.proximity > e.proximity))
    }

    if (scoutingAbandoned) {
      return
    }

    val scoutCount = Maff.vmin(
      With.blackboard.maximumScouts(),
      With.units.countOurs(IsWorker) - 3,
      basesToScout.size)
    if (lock.units.size > scoutCount) { lock.release() }

    scouts = lock
      .setCounter(CountUpTo(scoutCount))
      .setPreference(new PreferScout(basesToScout.map(_.townHallArea.center): _*))
      .acquire().toVector // The copy is important since the source is mutable

    if (scouts.isEmpty) return

    val enemyHasCombatUnits = With.units.enemy.exists(u => u.canAttack && ! IsWorker(u))
    val orderedScouts       = scouts.sortBy(s => basesToScout.view.map(_.townHallTile).map(s.pixelDistanceTravelling).min)
    val basesToScoutQueue   = new UnorderedBuffer[Base]()

    orderedScouts.indices.foreach(i => {
      if (basesToScoutQueue.isEmpty) {
        basesToScoutQueue.addAll(basesToScout)

        // For bookkeeping/debugging; vicinity is not actually used
        basesToScoutQueue.headOption.foreach(b => vicinity = b.heart.center)
      }
      val scout     = orderedScouts(i)
      val ovieBase  = With.tactics.scoutWithOverlord.vicinity.base.filter(b => With.tactics.scoutWithOverlord.units.nonEmpty)
      val base      = Maff
        .orElse(basesToScout.filter(_.isCross || i > 0 || ! With.blackboard.crossScout()), basesToScout)
        .minBy(b => scout.pixelDistanceTravelling(b.townHallTile) + 32 * 64 * Maff.fromBoolean(ovieBase.contains(b)))
      val bases     = Seq(base) ++ base.natural.filter(natural => ! enemyHasCombatUnits && ! base.tiles.exists(t => t.buildable && ! t.explored))
      val explored  = base.townHallArea.tiles.exists(_.explored)
      bases.foreach(basesToScoutQueue.remove)
      scout.intend(this)
        .setTerminus(base.townHallArea.cornerPixels.minBy(scout.pixelDistanceTravelling))
        .setScout(
          if (explored) bases.flatMap(_.tiles.view.filter(t => t.buildable && With.grids.enemyRangeGround(t) <= With.grids.enemyRangeGround.margin))
          else          Seq(base.townHallArea.tiles.minBy(scout.pixelDistanceTravelling)))
    })
  }
}

package Tactic.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Strategery.Strategies.Zerg.{ZvE4Pool, ZvT1HatchHydra}
import Utilities.?
import Utilities.Time.Seconds
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAny, IsComplete, IsWorker}
import Utilities.UnitPreferences.PreferScout

class SquadWorkerScout extends Squad {

  var scoutingAbandoned : Boolean                   = false
  var scouts            : Vector[FriendlyUnitInfo]  = Vector.empty
  val scoutLock         : LockUnits                 = new LockUnits(this, IsWorker).setInterruptible(false)

  def launch(): Unit = {
    val basesToScout = Maff.orElseFiltered(
      ?(With.scouting.enemyMainFullyScouted,
        With.geography.enemyBases.flatMap(b => Vector(b) ++ b.natural ++ b.naturalOf).distinct,
        With.scouting.enemyMain.toVector),
      With.geography.startBases.filterNot(_.scoutedByUs),
      With.geography.neutralBases)(! _.island).toVector

    scoutingAbandoned ||= scouts.exists( ! _.alive)
    scoutingAbandoned ||= basesToScout.isEmpty

    // Abandon scouting when we have a closer unit approaching
    scoutingAbandoned ||= With.blackboard.wantToAttack() && scouts.nonEmpty && With.units.ours.exists(u =>
      u.complete
      && ! IsWorker(u)
      && With.geography.enemyBases.view.map(_.heart).exists(h => scouts.forall(_.framesToTravelTo(h) > u.framesToTravelTo(h) - Seconds(5)())))

    // Abandon scouting when they can catch our scout (and we're not counting on the scout to help fight)
    scoutingAbandoned ||= ( ! ZvE4Pool() && ! ZvT1HatchHydra() && With.units.enemy.exists(_.isAll(
      IsComplete,
      IsAny(
        Terran.Marine,
        Terran.Vulture,
        Terran.Factory,
        Protoss.Dragoon,
        Protoss.PhotonCannon,
        Zerg.Hydralisk,
        Zerg.Mutalisk,
        Zerg.Spire))))

    // Abandon scouting when we have all the information we could possibly want
    scoutingAbandoned ||= With.fingerprints.fourPool()

    if (scoutingAbandoned) return

    val scoutCount = Maff.vmin(
      With.blackboard.maximumScouts(),
      With.units.countOurs(IsWorker) - 3,
      basesToScout.size)
    if (scoutLock.units.size > scoutCount) { scoutLock.release() }
    scoutLock.counter       = CountUpTo(scoutCount)
    scoutLock.preference    = new PreferScout(basesToScout.map(_.townHallArea.center): _*)
    scouts                  = scoutLock.acquire().toVector // The copy is important since the source is mutable
    if (scouts.isEmpty) return
    val enemyHasCombatUnits = With.units.enemy.exists(u => u.canAttack && ! IsWorker(u))
    val orderedScouts       = scouts.sortBy(s => basesToScout.view.map(_.townHallTile).map(s.pixelDistanceTravelling).min)
    val basesToScoutQueue   = new UnorderedBuffer[Base]()
    orderedScouts.indices.foreach(i => {
      if (basesToScoutQueue.isEmpty) {
        basesToScoutQueue.addAll(basesToScout)
      }
      val scout     = orderedScouts(i)
      val base      = Maff
        .orElse(basesToScout.filter(_.isCross || i > 0 || ! With.blackboard.crossScout()), basesToScout)
        .minBy(b => scout.pixelDistanceTravelling(b.townHallTile))
      val bases     = Seq(base) ++ base.natural.filter(natural => ! enemyHasCombatUnits && ! base.tiles.exists(t => t.buildable && ! t.explored))
      val explored  = base.townHallArea.tiles.exists(_.explored)
      bases.foreach(basesToScoutQueue.remove)
      scout.intend(this)
        .setTravel(base.townHallArea.cornerPixels.minBy(scout.pixelDistanceTravelling))
        .setScout(
          if (explored) bases.flatMap(_.tiles.view.filter(t => t.buildable && With.grids.enemyRangeGround(t) <= With.grids.enemyRangeGround.margin))
          else          Seq(base.townHallArea.tiles.minBy(scout.pixelDistanceTravelling)))
    })
    // For bookkeeping/debugging; vicinity is not actually used
    vicinity = orderedScouts.head.agent.destination
  }

  override def run(): Unit = {}
}

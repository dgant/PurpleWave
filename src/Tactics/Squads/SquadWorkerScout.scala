package Tactics.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountUpTo
import Planning.UnitMatchers.{MatchWarriors, MatchWorker, UnitMatcher}
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Strategery.Strategies.Zerg.{ZvE4Pool, ZvT1HatchHydra}
import Utilities.Seconds

class SquadWorkerScout extends Squad with Prioritized {

  var scouts: Iterable[FriendlyUnitInfo] = Iterable.empty
  var abandonScouting: Boolean = false
  val scoutLock: LockUnits = new LockUnits(this)
  scoutLock.interruptable = false
  scoutLock.matcher = MatchWorker

  def getScouts(matcher: UnitMatcher, count: Int): Iterable[FriendlyUnitInfo] = {
    if (scoutLock.units.size > count) { scoutLock.release() }
    scoutLock.counter = CountUpTo(count)
    scoutLock.preference = PreferIdle
    scoutLock.acquire(this)
    scoutLock.units
  }

  protected final def scoutBasesTowardsTownHall(unit: FriendlyUnitInfo, bases: Seq[Base]): Unit = {
    scoutTo(unit, bases, bases.maxBy(base => unit.pixelDistanceTravelling(base.townHallArea.center)).townHallArea.center)
  }

  private final def scoutTo(unit: FriendlyUnitInfo, bases: Seq[Base], destination: Pixel): Unit = {
    val tiles = bases.map(_.zone).distinct.flatMap(zone => zone.tiles.view.filter(tile =>
      With.grids.buildableTerrain.get(tile)
      && ! zone.bases.exists(_.harvestingArea.contains(tile)))) // Don't walk into worker line

    unit.intend(this, new Intention {
      toScoutTiles  = if (MatchWarriors(unit)) Seq.empty else tiles
      toTravel      = Some(destination)
    })
    bases.foreach(With.scouting.registerScout)
  }

  def recruit(): Unit = {
    lazy val unexploredStarts = With.geography.startBases.view.filterNot(_.townHallArea.tiles.exists(_.explored))
    lazy val basesToScout: Seq[Base] = Maff.orElse(
      With.geography.enemyBases.view.filterNot(_.zone.island).sortBy(_.isStartLocation).lastOption.toIterable,
      unexploredStarts.view.filterNot(_.zone.island),
      With.geography.neutralBases.view.filterNot(_.zone.island)).toSeq

    abandonScouting ||= scouts.exists( ! _.alive)
    abandonScouting ||= basesToScout.isEmpty

    // Abandon scouting when we have a closer unit approaching
    abandonScouting ||= With.blackboard.wantToAttack() && scouts.nonEmpty && With.units.ours.exists(u =>
      u.complete
      && ! MatchWorker(u)
      && With.geography.enemyBases.view.map(_.heart).exists(h => scoutLock.units.forall(_.framesToTravelTo(h) > u.framesToTravelTo(h) - Seconds(5)())))

    // Abandon scouting when they can catch our scout (and we're not counting on the scout to help fight)
    abandonScouting ||= ( ! ZvE4Pool.registerActive && ! ZvT1HatchHydra.registerActive && With.units.enemy.exists(u => u.complete && u.isAny(
      Terran.Marine,
      Terran.Vulture,
      Terran.Factory,
      Protoss.Dragoon,
      Protoss.PhotonCannon,
      Zerg.Hydralisk,
      Zerg.Mutalisk,
      Zerg.Spire)))

    // Abandon scouting when we have all the information we could possibly want
    abandonScouting ||= Seq(With.fingerprints.fourPool).exists(_.matches)

    if (abandonScouting) return

    var scoutCount = With.blackboard.maximumScouts()
    scoutCount = Maff.clamp(scoutCount, 1, basesToScout.size)
    scoutCount = Math.min(scoutCount, With.units.countOurs(MatchWorker) - 3)
    if (scoutCount < 1) return

    scouts = scoutLock.acquire(this)
    if (scouts.isEmpty) return

    val enemyHasCombatUnits = With.units.enemy.exists(u  => u.canAttack && ! MatchWorker(u))
    val orderedScouts = scouts.toVector.sortBy(s => basesToScout.view.map(_.townHallTile).map(s.pixelDistanceTravelling).min)
    val basesToScoutQueue = new UnorderedBuffer[Base](basesToScout)
    orderedScouts.indices.foreach(i => {
      if (basesToScoutQueue.isEmpty) {
        basesToScoutQueue.addAll(basesToScout)
      }
      val scout     = orderedScouts(i)
      val base      = basesToScout.minBy(b => scout.pixelDistanceTravelling(b.townHallTile))
      val bases     = if (enemyHasCombatUnits || base.tiles.exists(t => t.buildable && ! t.explored) || base.natural.isEmpty) Seq(base) else Seq(base, base.natural.get)
      val explored  = base.townHallArea.tiles.exists(_.explored)
      bases.foreach(basesToScoutQueue.remove)
      scout.intend(this, new Intention{
        toScoutTiles =
          if (explored) bases.flatMap(_.tiles.view.filter(t => t.buildable && With.grids.enemyRangeGround(t) <= With.grids.enemyRangeGround.margin))
          else Seq(base.townHallArea.tiles.minBy(scout.pixelDistanceTravelling))
      })
    })
  }

  override def run(): Unit = {

  }
}

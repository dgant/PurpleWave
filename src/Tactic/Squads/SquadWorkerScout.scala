package Tactic.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Strategery.Strategies.Zerg.{ZvE4Pool, ZvT1HatchHydra}
import Utilities.?
import Utilities.Time.Seconds
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsWarrior, IsWorker}
import Utilities.UnitPreferences.PreferScout

class SquadWorkerScout extends Squad {

  var scouts: Vector[FriendlyUnitInfo] = Vector.empty
  var abandonScouting: Boolean = false
  val scoutLock: LockUnits = new LockUnits(this)
  scoutLock.interruptable = false
  scoutLock.matcher = IsWorker

  protected final def scoutBasesTowardsTownHall(unit: FriendlyUnitInfo, bases: Seq[Base]): Unit = {
    scoutTo(unit, bases, bases.maxBy(base => unit.pixelDistanceTravelling(base.townHallArea.center)).townHallArea.center)
  }

  private final def scoutTo(unit: FriendlyUnitInfo, bases: Seq[Base], destination: Pixel): Unit = {
    val tiles = bases.map(_.zone).distinct.flatMap(zone => zone.tiles.view.filter(tile =>
      With.grids.buildableTerrain.get(tile)
      && ! zone.bases.exists(_.harvestingArea.contains(tile)))) // Don't walk into worker line

    unit.intend(this)
      .setScout(?(IsWarrior(unit), Seq.empty, tiles))
      .setTravel(destination)
    bases.foreach(With.scouting.registerScout)
  }

  def launch(): Unit = {
    lazy val unexploredStarts = With.geography.startBases.view.filterNot(_.scoutedByUs)
    lazy val basesToScout: Seq[Base] = Maff.orElse(
      if (With.scouting.enemyMainFullyScouted)
        With.geography.enemyBases.view.filterNot(_.zone.island).flatMap(b => Vector(b) ++ b.natural ++ b.naturalOf).distinct
      else
        With.scouting.enemyMain,
      unexploredStarts.view.filterNot(_.zone.island),
      With.geography.neutralBases.view.filterNot(_.zone.island)).toSeq

    abandonScouting ||= scouts.exists( ! _.alive)
    abandonScouting ||= basesToScout.isEmpty

    // Abandon scouting when we have a closer unit approaching
    abandonScouting ||= With.blackboard.wantToAttack() && scouts.nonEmpty && With.units.ours.exists(u =>
      u.complete
      && ! IsWorker(u)
      && With.geography.enemyBases.view.map(_.heart).exists(h => scouts.forall(_.framesToTravelTo(h) > u.framesToTravelTo(h) - Seconds(5)())))

    // Abandon scouting when they can catch our scout (and we're not counting on the scout to help fight)
    abandonScouting ||= ( ! ZvE4Pool() && ! ZvT1HatchHydra() && With.units.enemy.exists(u => u.complete && u.isAny(
      Terran.Marine,
      Terran.Vulture,
      Terran.Factory,
      Protoss.Dragoon,
      Protoss.PhotonCannon,
      Zerg.Hydralisk,
      Zerg.Mutalisk,
      Zerg.Spire)))

    // Abandon scouting when we have all the information we could possibly want
    abandonScouting ||= With.fingerprints.fourPool()

    if (abandonScouting) return

    val scoutCount = Math.min(
      Math.min(
        With.blackboard.maximumScouts(),
        With.units.countOurs(IsWorker) - 3),
        basesToScout.size)
    if (scoutCount < 1) return

    if (scoutLock.units.size > scoutCount) { scoutLock.release() }
    scoutLock.counter = CountUpTo(scoutCount)
    scoutLock.preference = new PreferScout(basesToScout.map(_.townHallArea.center): _*)
    scouts = scoutLock.acquire().toVector // The copy is important since the source is mutable
    if (scouts.isEmpty) return

    val enemyHasCombatUnits = With.units.enemy.exists(u  => u.canAttack && ! IsWorker(u))
    val orderedScouts = scouts.sortBy(s => basesToScout.view.map(_.townHallTile).map(s.pixelDistanceTravelling).min)
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
      scout.intend(this)
        .setTravel(base.townHallArea.cornerPixels.minBy(scout.pixelDistanceTravelling))
        .setScout(
          if (explored) bases.flatMap(_.tiles.view.filter(t => t.buildable && With.grids.enemyRangeGround(t) <= With.grids.enemyRangeGround.margin))
          else Seq(base.townHallArea.tiles.minBy(scout.pixelDistanceTravelling)))
    })
  }

  override def run(): Unit = {}
}

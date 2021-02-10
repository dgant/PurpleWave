package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountBetween, CountOne}
import Planning.UnitMatchers.{MatchMobile, UnitMatcher}
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class DoScout extends Prioritized {

  protected def replaceDeadScouts: Boolean = true
  protected final def enemyFound: Boolean = With.scouting.enemyMain.isDefined

  protected val scoutLock: LockUnits = new LockUnits
  scoutLock.matcher.set(MatchMobile)
  scoutLock.counter.set(CountOne)
  scoutLock.preference.set(PreferIdle)
  scoutLock.interruptable.set(false)

  protected final def getScouts(matcher: UnitMatcher, count: Int): Iterable[FriendlyUnitInfo] = {
    if (scoutLock.units.size > count) {
      scoutLock.release()
    }
    scoutLock.matcher.set(matcher)
    scoutLock.counter.set(new CountBetween(0, count))
    scoutLock.preference.set(PreferIdle)
    scoutLock.acquire(this)
    scoutLock.units
  }

  protected final def scoutBasesTowardsTownHall(unit: FriendlyUnitInfo, bases: Seq[Base]): Unit = {
    scoutTo(unit, bases, bases.maxBy(base => unit.pixelDistanceTravelling(base.townHallArea.midPixel)).townHallArea.midPixel)
  }

  protected final def scoutBaseTowardsEntrance(unit: FriendlyUnitInfo, bases: Seq[Base]): Unit = {
    val base = bases.minBy(base => unit.pixelDistanceTravelling(base.townHallArea.midPixel))
    scoutTo(unit, bases, base.zone.exit.map(_.pixelCenter).getOrElse(base.townHallArea.midPixel))
  }

  private final def scoutTo(unit: FriendlyUnitInfo, bases: Seq[Base], destination: Pixel): Unit = {
    val tiles = bases.map(_.zone).distinct.flatMap(zone => zone.tiles.view.filter(tile =>
      With.grids.buildableTerrain.get(tile)
      && ! zone.bases.exists(_.harvestingArea.contains(tile)))) // Don't walk into worker line

    unit.agent.intend(this, new Intention {
      toScoutTiles  = tiles
      toTravel      = Some(destination)
      canFocus      = true
    })
    bases.foreach(With.scouting.registerScout)
  }
}

package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountBetween, UnitCountOne}
import Planning.UnitMatchers.{MatchMobile, Matcher}
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class DoScout extends Prioritized {

  protected def replaceDeadScouts: Boolean = true
  protected final def enemyFound: Boolean = With.scouting.enemyMain.isDefined

  protected val scoutLock: LockUnits = new LockUnits
  scoutLock.unitMatcher.set(MatchMobile)
  scoutLock.unitCounter.set(UnitCountOne)
  scoutLock.unitPreference.set(PreferIdle)
  scoutLock.interruptable.set(false)

  protected final def getScouts(matcher: Matcher, count: Int): Iterable[FriendlyUnitInfo] = {
    if (scoutLock.units.size > count) {
      scoutLock.release()
    }
    scoutLock.unitMatcher.set(matcher)
    scoutLock.unitCounter.set(new UnitCountBetween(0, count))
    scoutLock.unitPreference.set(PreferIdle)
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

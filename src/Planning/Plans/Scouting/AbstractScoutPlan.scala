package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountBetween, UnitCountOne}
import Planning.UnitMatchers.{UnitMatchMobile, UnitMatcher}
import Planning.UnitPreferences.UnitPreferIdle
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class AbstractScoutPlan extends Plan {

  protected def replaceDeadScouts: Boolean = true
  protected final def enemyFound: Boolean = With.scouting.enemyMain.isDefined

  protected val scoutLock = new LockUnits {
    unitMatcher.set(UnitMatchMobile)
    unitCounter.set(UnitCountOne)
    unitPreference.set(UnitPreferIdle)
  }

  protected final def getScouts(matcher: UnitMatcher, count: Int): Iterable[FriendlyUnitInfo] = {
    scoutLock.unitMatcher.set(matcher)
    scoutLock.unitCounter.set(new UnitCountBetween(0, count))
    scoutLock.unitPreference.set(UnitPreferIdle)
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
    unit.agent.intend(this, new Intention {
      canScout      = true
      toScoutBases  = bases
      toTravel      = Some(destination)
    })
    bases.foreach(With.scouting.registerScout)
  }
}

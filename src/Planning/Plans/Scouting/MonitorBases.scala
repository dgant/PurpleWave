package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountOne, UnitCounter}
import Planning.UnitMatchers.Matcher
import Planning.UnitPreferences.PreferClose
import Planning.{Plan, Property}

class MonitorBases(
                    initialUnitMatcher: Matcher,
                    bases: () => Seq[Base] = () => With.geography.enemyBases,
                    initialUnitCounter: UnitCounter = UnitCountOne) extends Plan {

  val scouts = new Property[LockUnits](new LockUnits {
    unitMatcher.set(initialUnitMatcher)
    unitCounter.set(initialUnitCounter)
    interruptable.set(false)
  })

  override def onUpdate(): Unit = {
    val basesToScout = bases()
    if (basesToScout.isEmpty) return
    scouts.get.unitPreference.set(PreferClose(scouts.get.units.headOption.map(_.pixel).getOrElse(With.geography.home.pixelCenter)))
    scouts.get.acquire(this)
    scouts.get.units.foreach(scout => scout.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      toScoutTiles = basesToScout.flatMap(_.zone.tiles)
    }))
  }
}

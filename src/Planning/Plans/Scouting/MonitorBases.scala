package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountOne, UnitCounter}
import Planning.UnitMatchers.UnitMatcher
import Planning.UnitPreferences.PreferClose

class MonitorBases(
  initialUnitMatcher: UnitMatcher,
  bases: () => Seq[Base] = () => With.geography.enemyBases,
  initialUnitCounter: UnitCounter = CountOne) extends Plan {

  val scouts = new LockUnits(this)
  scouts.matcher = initialUnitMatcher
  scouts.counter = initialUnitCounter
  scouts.interruptable = false

  override def onUpdate(): Unit = {
    val basesToScout = bases()
    if (basesToScout.isEmpty) return
    scouts.preference = PreferClose(scouts.units.headOption.map(_.pixel).getOrElse(With.geography.home.pixelCenter))
    scouts.acquire(this)
    scouts.units.foreach(scout => scout.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      toScoutTiles = basesToScout.flatMap(_.zone.tiles)
    }))
  }
}

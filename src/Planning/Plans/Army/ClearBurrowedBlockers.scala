package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchMobileDetectors, UnitMatchOr}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.{Minutes, Seconds}

import scala.util.Random

class ClearBurrowedBlockers extends Plan {
  
  val detector = new Property(new LockUnits)
  detector.get.unitMatcher.set(UnitMatchMobileDetectors)
  detector.get.unitCounter.set(UnitCountOne)
  
  val clearer = new Property(new LockUnits)
  clearer.get.unitMatcher.set(UnitMatchOr(Terran.Marine, Terran.Firebat, Terran.Goliath, Terran.Wraith, Protoss.Zealot, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk, Zerg.Mutalisk))
  clearer.get.unitCounter.set(UnitCountOne)

  override def onUpdate(): Unit = {

    if (With.frame < Minutes(6)()) {
      return
    }

    if ( ! With.enemies.exists(_.isZerg) && ! With.enemies.exists(_.isTerran)) {
      return
    }
    if ( ! With.enemies.exists(_.isZerg) && With.enemies.forall(With.unitsShown(_, Terran.SpiderMine) == 0)) {
      return
    }

    val target = With.units.ours
      .find(u => u.agent.toBuild.exists(_.isTownHall) && ! u.unitClass.isBuilding)
      .flatMap(_.agent.toBuildTile.map(_.pixelCenter))

    if (target.isEmpty) return

    detector.get.unitPreference.set(UnitPreferClose(target.get))
    detector.get.acquire(this)
    detector.get.units.foreach(_.agent.intend(this, new Intention {
      toTravel = target
      canFocus = true
    }))

    if (With.enemies.exists(_.isZerg) || detector.get.units.forall(_.framesToTravelTo(target.get) > Seconds(5)())) {
      clearer.get.unitPreference.set(UnitPreferClose(target.get))
      clearer.get.acquire(this)
      clearer.get.units.foreach(_.agent.intend(this, new Intention {
        toTravel = Some(target.get.add(Random.nextInt(160) - 80, Random.nextInt(128) - 64))
        canFocus = true
      }))
    }
  }
}

package Planning.Plans.Army

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchCustom, UnitMatchMobileDetectors}
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.Terran

import scala.util.Random

class ClearBurrowedBlockers extends Plan {
  
  val detector = new Property(new LockUnits)
  detector.get.unitMatcher.set(UnitMatchMobileDetectors)
  detector.get.unitCounter.set(UnitCountOne)
  
  val decoy = new Property(new LockUnits)
  decoy.get.unitMatcher.set(UnitMatchCustom((unit) => unit.canMove && unit.unitClass.triggersSpiderMines))
  decoy.get.unitCounter.set(UnitCountOne)
  
  override def onUpdate(): Unit = {
    
    if ( ! With.enemies.exists(_.isZerg) && ! With.enemies.exists(_.isTerran)) {
      return
    }
    if ( ! With.enemies.exists(_.isZerg) && With.enemies.forall(With.intelligence.unitsShown(_, Terran.SpiderMine) == 0)) {
      return
    }
    
    val target = With.units.ours
      .find(u => u.agent.toBuild.exists(_.isTownHall))
      .flatMap(_.agent.toBuildTile.map(_.pixelCenter))
    
    if (target.isEmpty) return
    
    detector.get.unitPreference.set(UnitPreferClose(target.get))
    detector.get.acquire(this)
    if (detector.get.units.nonEmpty) {
      detector.get.units.foreach(_.agent.intend(this, new Intention {
        toTravel = target
      }))
    }
  
    if (With.enemies.exists(_.isTerran) && detector.get.units.forall(_.framesToTravelTo(target.get) > GameTime(0, 5)())) {
      decoy.get.unitPreference.set(UnitPreferClose(target.get))
      decoy.get.acquire(this)
      decoy.get.units.foreach(_.agent.intend(this, new Intention {
        toTravel = Some(target.get.add(Random.nextInt(160) - 80, Random.nextInt(128) - 64))
      }))
    }
  }
}

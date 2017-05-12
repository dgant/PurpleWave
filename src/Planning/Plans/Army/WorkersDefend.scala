package Planning.Plans.Army

import Information.Battles.Types.Battle
import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchSpecific
import Planning.Plan
import ProxyBwapi.UnitInfo.UnitInfo

class WorkersDefend extends Plan {
  
  description.set("Workers defending")
  
  private val matchWorkers = new UnitMatchSpecific
  
  val workers = new LockUnits {
    unitMatcher.set(matchWorkers)
    unitCounter.set(UnitCountEverything)
  }
  
  override def update() {
    //Un-implemented for now while I figure out a better way to determine how many workers should fight, when, and where
    matchWorkers.specificUnits = Set.empty
    workers.release()
    workers.acquire(this)
    workers.units.foreach(worker => With.executor.intend(new Intention(this, worker)))
  }
  
  private def battleWorkers(battle:Battle):Traversable[UnitInfo] = {
    battle.us.units.filter(_.unitClass.isWorker)
  }
}

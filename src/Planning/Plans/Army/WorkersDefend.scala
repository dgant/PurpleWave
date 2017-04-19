package Planning.Plans.Army

import Information.Battles.Types.{Battle, Tactics}
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
    matchWorkers.specificUnits =
      With.battles.local.flatten(battle =>
        if (battle.bestSimulationResult.exists(_.us.tactics.has(Tactics.Workers.FightAll))) {
          battleUnits(battle)
        }
        else if (battle.bestSimulationResult.exists(_.us.tactics.has(Tactics.Workers.FightHalf))) {
          //Hacky, because it ignores whether we'll actually get these units
          //Better would be to use a preference and a counter
          val workers = battleUnits(battle)
          workers.toVector.sortBy(_.totalHealth < 20).take(workers.size/2)
        }
        else {
          Vector.empty
        }).toSet
    
    workers.release()
    workers.acquire(this)
    workers.units.foreach(worker => With.executor.intend(new Intention(this, worker)))
  }
  
  private def battleUnits(battle:Battle):Traversable[UnitInfo] = {
    battle.us.units.filter(_.unitClass.isWorker)
  }
}

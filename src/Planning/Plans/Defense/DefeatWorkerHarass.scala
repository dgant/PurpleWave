package Planning.Plans.Defense

import Micro.Behaviors.DefaultBehavior
import Planning.Plans.Allocation.LockUnits
import Planning.Plan
import Startup.With
import Planning.Composition.PositionFinders.PositionSpecific
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Composition.UnitPreferences.UnitPreferClose
import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class DefeatWorkerHarass extends Plan {
  
  private val defenders = new mutable.HashMap[UnitInfo, LockUnits]
  
  override def getChildren: Iterable[Plan] = defenders.values
  
  override def onFrame() {
    
    val intruders = With.geography.ourHarvestingAreas
      .flatten(With.units.inRectangle)
      .filter(_.isEnemy)
      .filter(_.canFight)
      .filter(_.visible)
      .filter(! _.flying)
      .toSet
    
    defenders.keySet.diff(intruders).foreach(defenders.remove)
    intruders.diff(defenders.keySet).foreach(defendFromEnemy)
    defenders.values.foreach(defenders => {
      defenders.onFrame
      if (defenders.isComplete) {
        defenders.units.foreach(defender => With.executor.intend(new Intention(this, defender, DefaultBehavior, defender.tileCenter)))
      }
    })
  }
  
  private def defendFromEnemy(enemy:UnitInfo) {
    if ( ! defenders.contains(enemy)) {
      defenders.put(enemy, new LockUnits {
        unitMatcher.set(UnitMatchWorker)
        unitPreference.set(new UnitPreferClose { positionFinder.set(new PositionSpecific(enemy.tileCenter)) })
        unitCounter.set(new UnitCountBetween(1, 2))
      })
    }
  }
}

package Plans.Defense

import Global.Combat.Commands.Engage
import Plans.Allocation.LockUnits
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionSpecific
import Strategies.UnitCounters.UnitCountBetween
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import Types.Intents.Intention
import Types.UnitInfo.UnitInfo

import scala.collection.mutable

class DefeatWorkerHarass extends Plan {
  
  val _defenders = new mutable.HashMap[UnitInfo, LockUnits]
  
  override def getChildren: Iterable[Plan] = _defenders.values
  
  override def onFrame() {
    
    val intruders = With.geography.ourHarvestingAreas
      .flatten(With.units.inRectangle)
      .filter(_.isEnemy)
      .filter(_.canFight)
      .filter(_.visible)
      .filter(! _.flying)
      .toSet
    
    _defenders.keySet.diff(intruders).foreach(_defenders.remove)
    intruders.diff(_defenders.keySet).foreach(_defendFromEnemy)
    _defenders.values.foreach(defenders => {
      defenders.onFrame
      if (defenders.isComplete) {
        defenders.units.foreach(defender => With.commander.intend(new Intention(this, defender, Engage, defender.tileCenter)))
      }
    })
  }
  
  def _defendFromEnemy(enemy:UnitInfo) {
    if ( ! _defenders.contains(enemy)) {
      _defenders.put(enemy, new LockUnits {
        unitMatcher.set(UnitMatchWorker)
        unitPreference.set(new UnitPreferClose { positionFinder.set(new PositionSpecific(enemy.tileCenter)) })
        unitCounter.set(new UnitCountBetween(1, 2))
      })
    }
  }
}

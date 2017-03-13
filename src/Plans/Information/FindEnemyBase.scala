package Plans.Information

import Global.Combat.Behaviors.DefaultBehavior
import Plans.Allocation.LockUnits
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionCenter
import Strategies.UnitCounters.UnitCountExactly
import Strategies.UnitMatchers.UnitMatchMobile
import Strategies.UnitPreferences.UnitPreferClose
import Types.Intents.Intention
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property
import bwapi.TilePosition

class FindEnemyBase extends Plan {
  
  description.set("Discover an enemy base")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(new UnitCountExactly(1))
    unitMatcher.set(new UnitMatchMobile)
    unitPreference.set(new UnitPreferClose { positionFinder.set(new PositionCenter) })
  })
  
  override def isComplete: Boolean = With.intelligence.mostBaselikeEnemyBuilding.nonEmpty
  override def getChildren: Iterable[Plan] = List(scouts.get)
  
  override def onFrame() {
    scouts.get.onFrame()
    scouts.get.units.foreach(_orderScout)
  }
  
  def _orderScout(scout:FriendlyUnitInfo) =
    With.commander.intend(new Intention(this, scout, DefaultBehavior, _getNextScoutingPosition))
  
  def _getNextScoutingPosition:TilePosition = {
    With.intelligence.leastScoutedBases
      .filter(base => With.paths.exists(With.geography.home, base)) //BWTA.isConnected could also help
      .head
  }
}

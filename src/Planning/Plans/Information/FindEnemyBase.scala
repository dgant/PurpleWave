package Planning.Plans.Information

import Micro.Behaviors.DefaultBehavior
import Planning.Plans.Allocation.LockUnits
import Planning.Plan
import Startup.With
import Planning.Composition.PositionFinders.PositionCenter
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Composition.UnitPreferences.UnitPreferClose
import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.Composition.Property
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
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout:FriendlyUnitInfo) =
    With.commander.intend(new Intention(this, scout, DefaultBehavior, getNextScoutingPosition))
  
  private def getNextScoutingPosition:TilePosition = {
    With.intelligence.leastScoutedBases
      .filter(base => With.paths.exists(With.geography.home, base)) //BWTA.isConnected could also help
      .head
  }
}

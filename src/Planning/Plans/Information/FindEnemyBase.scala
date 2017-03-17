package Planning.Plans.Information

import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.PositionCenter
import Planning.Composition.Property
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Startup.With
import bwapi.TilePosition

class FindEnemyBase extends Plan {
  
  description.set("Discover an enemy base")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(new UnitCountExactly(1))
    unitMatcher.set(UnitMatchMobile)
    unitPreference.set(new UnitPreferClose { positionFinder.set(PositionCenter) })
  })
  
  override def isComplete: Boolean = With.intelligence.mostBaselikeEnemyBuilding.nonEmpty
  override def getChildren: Iterable[Plan] = List(scouts.get)
  
  override def onFrame() {
    scouts.get.onFrame()
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout:FriendlyUnitInfo) =
    With.executor.intend(new Intention(this, scout) { destination = Some(getNextScoutingPosition) })
  
  private def getNextScoutingPosition:TilePosition = {
    With.intelligence.leastScoutedBases
      .filter(base => With.paths.exists(With.geography.home, base)) //BWTA.isConnected could also help
      .head
  }
}

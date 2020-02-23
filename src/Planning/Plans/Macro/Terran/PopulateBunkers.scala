package Planning.Plans.Macro.Terran

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class PopulateBunkers extends Plan {
  
  val bunkerLocks = new mutable.HashMap[FriendlyUnitInfo, LockUnits]
  
  override def onUpdate() {
    val bunkers = With.units.ours.filter(_.is(Terran.Bunker)).toSet
    
    bunkerLocks.keySet.diff(bunkers).foreach(bunkerLocks.remove)
    bunkers.diff(bunkerLocks.keySet).foreach(bunker => bunkerLocks(bunker) = newLock(bunker))
    
    bunkerLocks.keys.toVector.sortBy(_.id).foreach(updateBunker)
  }
  
  private def newLock(bunker: FriendlyUnitInfo): LockUnits = {
    val output = new LockUnits
    output.unitMatcher.set(Terran.Marine)
    output.unitCounter.set(new UnitCountBetween(1, 4))
    output.unitPreference.set(UnitPreferClose(bunker.pixelCenter))
    output
  }
  
  private def updateBunker(bunker: FriendlyUnitInfo) {
    val lock = bunkerLocks(bunker)
    lock.acquire(this)
    lock.units.foreach(unit => {
      val intent = new Intention
      intent.toTravel = Some(bunker.pixelCenter)
      intent.toBoard = Some(bunker)
      unit.agent.intend(this, intent)
    })
  }
}

package Planning.Plans.Macro.Terran

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountUpTo
import Planning.UnitPreferences.PreferClose
import Planning.Plan
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class PopulateBunkers extends Plan {
  
  val bunkerLocks = new mutable.HashMap[FriendlyUnitInfo, LockUnits]
  
  override def onUpdate() {
    val bunkers = With.units.ours.filter(Terran.Bunker).toSet
    
    bunkerLocks.keySet.diff(bunkers).foreach(bunkerLocks.remove)
    bunkers.diff(bunkerLocks.keySet).foreach(bunker => bunkerLocks(bunker) = newLock(bunker))
    
    bunkerLocks.keys.toVector.sortBy(_.id).foreach(updateBunker)
  }
  
  private def newLock(bunker: FriendlyUnitInfo): LockUnits = {
    val output = new LockUnits(this)
    output.matcher = Terran.Marine
    output.counter = CountUpTo(4)
    output.preference = PreferClose(bunker.pixel)
    output
  }
  
  private def updateBunker(bunker: FriendlyUnitInfo) {
    val lock = bunkerLocks(bunker)
    lock.acquire(this)
    lock.units.foreach(unit => {
      val intent = new Intention
      intent.toTravel = Some(bunker.pixel)
      intent.toBoard = Some(bunker)
      unit.intend(this, intent)
    })
  }
}

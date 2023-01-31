package Planning.Plans.Macro.Terran

import Lifecycle.With
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitPreferences.PreferClose

import scala.collection.mutable

class PopulateBunkers extends Plan {
  
  val bunkerLocks = new mutable.HashMap[FriendlyUnitInfo, LockUnits]
  
  override def onUpdate(): Unit = {
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
  
  private def updateBunker(bunker: FriendlyUnitInfo): Unit = {
    val lock = bunkerLocks(bunker)
    lock.acquire()
    lock.units.foreach(_.intend(this).setTravel(bunker.pixel).setBoard(bunker))
  }
}

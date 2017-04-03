package Macro.Allocation

import Geometry.TileRectangle
import Planning.Composition.ResourceLocks.LockArea

import scala.collection.mutable

class RealEstate {
  
  val requests = new mutable.HashSet[LockArea]
  
  def onFrame() {
    requests.clear()
  }
  
  def request(lock:LockArea): Boolean = {
    requests.remove(lock)
    val approved = available(lock.area)
    
    if (approved) {
      requests.add(lock)
    }
    
    return approved
  }
  
  def available(rectangle:TileRectangle):Boolean = {
    ! requests.exists(_.area.intersects(rectangle))
  }
  
  def reserved:Iterable[TileRectangle] = requests.map(_.area)
  
  def release(lock:LockArea) {
    requests.remove(lock)
  }
}

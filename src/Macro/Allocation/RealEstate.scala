package Macro.Allocation

import Mathematics.Points.TileRectangle
import Planning.Composition.ResourceLocks.LockArea

import scala.collection.mutable

class RealEstate {
  
  val requests = new mutable.HashSet[LockArea]
  
  def update() {
    requests.clear()
  }
  
  def request(lock:LockArea): Boolean = {
    requests.remove(lock)
    val approved = lock.area.isDefined && available(lock.area.get)
    
    if (approved) {
      requests.add(lock)
    }
    
    return approved
  }
  
  def available(rectangle:TileRectangle):Boolean = {
    ! requests.exists(_.area.get.intersects(rectangle))
  }
  
  def reserved:Iterable[TileRectangle] = requests.map(_.area.get)
  
  def release(lock:LockArea) {
    requests.remove(lock)
  }
}

package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Pixels.Point
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class AbstractGridPsi extends AbstractGridBoolean {
  
  private var lastPylons:Set[FriendlyUnitInfo] = Set.empty
  
  override def update(): Unit = {
    
    val newPylons = pylons
    if (newPylons == lastPylons) return
    lastPylons = newPylons
    
    reset()
    pylons.foreach(pylon =>
      psiPoints.foreach(point =>
        set(pylon.tileTopLeft.add(point), true)))
  }
  
  val psiPoints:Array[Point]
  def pylons:Set[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.aliveAndComplete && unit.is(Protoss.Pylon))
}

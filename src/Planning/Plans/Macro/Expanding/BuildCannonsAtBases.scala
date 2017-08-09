package Planning.Plans.Macro.Expanding

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Composition.Property
import Planning.Plan
import ProxyBwapi.Races.Protoss

class BuildCannonsAtBases(initialCount: Int) extends Plan {
  
  val count: Property[Int] = new Property(initialCount)
  
  override def onUpdate() {
    val bases = eligibleBases
    if (eligibleBases.nonEmpty) {
      if (With.units.ours.exists(_.is(Protoss.Forge))) {
        eligibleBases.foreach(cannonBase)
      }
      else {
        With.scheduler.request(this, RequestAtLeast(1, Protoss.Forge))
      }
    }
  }
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
  }
  
  private def cannonBase(base: Base) {
    val zone                = base.zone
    val cannonsInExpansion  = With.units.ours.count(unit => unit.is(Protoss.PhotonCannon) && unit.pixelCenter.zone == zone)
    val cannonsToAdd        = count.get - cannonsInExpansion
    val pylonBlueprint      = new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(base.zone))
    
    if (cannonsToAdd <= 0) return
    
    With.groundskeeper.propose(pylonBlueprint)
    for (i <- 0 to cannonsToAdd) {
      val cannonBlueprint = new Blueprint(this, building = Some(Protoss.PhotonCannon), requireZone = Some(base.zone))
      With.groundskeeper.propose(cannonBlueprint)
    }
    With.scheduler.request(this, RequestAnother(cannonsToAdd, Protoss.PhotonCannon))
  }
}

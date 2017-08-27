package Planning.Plans.Macro.Expanding

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Composition.Property
import Planning.Plan
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtBases(initialCount: Int) extends Plan {
  
  val count: Property[Int] = new Property(initialCount)
  
  override def onUpdate() {
    val bases = eligibleBases
    if (eligibleBases.nonEmpty) {
      if (With.units.ours.exists(_.is(Terran.EngineeringBay))) {
        eligibleBases.foreach(turretBase)
      }
      else {
        With.scheduler.request(this, RequestAtLeast(1, Terran.EngineeringBay))
      }
    }
  }
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
  }
  
  private def turretBase(base: Base) {
    val zone          = base.zone
    val turretsInBase = With.units.ours.count(unit => unit.is(Terran.MissileTurret) && unit.zone == zone)
    val turretsToAdd  = count.get - turretsInBase
    
    if (turretsToAdd <= 0) return
    
    for (i <- 0 to turretsToAdd) {
      val blueprint = new Blueprint(this, building = Some(Terran.MissileTurret), requireZone = Some(base.zone), placement = Some(PlacementProfiles.hugTownHall))
      With.groundskeeper.propose(blueprint)
    }
    With.scheduler.request(this, RequestAnother(turretsToAdd, Terran.MissileTurret))
  }
}

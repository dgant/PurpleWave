package Planning.Plans.Macro.Terran

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{Get, GetAnother}
import Planning.{Plan, Property}
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtBases(initialCount: Int) extends Plan {
  
  val count: Property[Int] = new Property(initialCount)
  
  override def onUpdate() {
    val bases = eligibleBases
    if (eligibleBases.nonEmpty) {
      if (With.units.existsOurs(Terran.EngineeringBay)) {
        eligibleBases.foreach(turretBase)
      }
      else {
        With.scheduler.request(this, Get(1, Terran.EngineeringBay))
      }
    }
  }
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
  }
  
  private def turretBase(base: Base) {
    val zone          = base.zone
    val turretsInBase = With.units.countOurs(unit => unit.is(Terran.MissileTurret) && unit.zone == zone)
    val turretsToAdd  = count.get - turretsInBase
    
    if (turretsToAdd <= 0) return
    
    for (i <- 0 to turretsToAdd) {
      val blueprint = new Blueprint(this, building = Some(Terran.MissileTurret), requireZone = Some(base.zone), placement = Some(PlacementProfiles.hugTownHall))
      With.groundskeeper.propose(blueprint)
    }
    With.scheduler.request(this, GetAnother(turretsToAdd, Terran.MissileTurret))
  }
}

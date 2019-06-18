package Planning.Plans.Placement

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.{Get, GetAnother}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete}
import Planning.{Plan, Property}
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtBases(
  initialCount: Int,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
    extends Plan {
  
  val count: Property[Int] = new Property(initialCount)
  
  override def onUpdate() {
    val bases = eligibleBases
    if (eligibleBases.nonEmpty) {
      if (With.units.existsOurs(UnitMatchAnd(Terran.EngineeringBay, UnitMatchComplete))) {
        eligibleBases.foreach(turretBase)
      }
      With.scheduler.request(this, Get(Terran.EngineeringBay))
    }
  }
  
  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBases
  }
  
  private def turretBase(base: Base) {
    val zone          = base.zone
    val turretsInBase = With.units.countOursP(unit => unit.is(Terran.MissileTurret) && unit.zone == zone)
    val turretsToAdd  = count.get - turretsInBase
    
    if (turretsToAdd <= 0) return
    
    for (i <- 0 to turretsToAdd) {
      val blueprint = new Blueprint(Terran.MissileTurret, requireZone = Some(base.zone), placement = Some(placement))
      With.groundskeeper.suggest(blueprint)
    }
    With.scheduler.request(this, GetAnother(turretsToAdd, Terran.MissileTurret))
  }
}

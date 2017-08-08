package Planning.Plans.Macro.Expanding

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Composition.Property
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran}

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
    val zone = base.zone
    val turretsInBase = With.units.ours.filter(unit => unit.is(Terran.MissileTurret) && unit.pixelCenter.zone == zone)
    for (i <- 0 to count.get) {
      val blueprint = new Blueprint(this, building = Some(Protoss.PhotonCannon), requireZone = Some(base.zone))
      With.groundskeeper.propose(blueprint)
    }
    With.scheduler.request(this, RequestAnother(count.get - turretsInBase.size, Terran.MissileTurret))
  }
}

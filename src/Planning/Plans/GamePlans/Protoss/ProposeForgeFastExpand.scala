package Planning.Plans.GamePlans.Protoss

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Planning.Plan
import ProxyBwapi.Races.Protoss

class ProposeForgeFastExpand extends Plan {
  
  lazy val descriptors = Vector(
    new BuildingDescriptor(this, argBuilding = Some(Protoss.Pylon),         zone = With.geography.ourNatural.map(_.zone)),
    new BuildingDescriptor(this, argBuilding = Some(Protoss.Forge),         zone = With.geography.ourNatural.map(_.zone)),
    new BuildingDescriptor(this, argBuilding = Some(Protoss.PhotonCannon),  zone = With.geography.ourNatural.map(_.zone))
  )
  
  override def onUpdate() {
    descriptors.foreach(With.groundskeeper.propose)
  }
}

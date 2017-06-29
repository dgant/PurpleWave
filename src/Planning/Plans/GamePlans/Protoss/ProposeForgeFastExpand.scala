package Planning.Plans.GamePlans.Protoss

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Planning.Plans.Compound.Parallel
import Planning.Plans.Macro.Build.ProposePlacement
import ProxyBwapi.Races.Protoss

class ProposeForgeFastExpand extends Parallel {
  
  children.set(
    Vector(
      new BuildingDescriptor(this, argBuilding = Some(Protoss.Pylon), zone = With.geography.ourNatural.map(_.zone)),
      new BuildingDescriptor(this, argBuilding = Some(Protoss.Forge), zone = With.geography.ourNatural.map(_.zone)),
      new BuildingDescriptor(this, argBuilding = Some(Protoss.PhotonCannon), zone = With.geography.ourNatural.map(_.zone)))
    .map(new ProposePlacement(_)))
}

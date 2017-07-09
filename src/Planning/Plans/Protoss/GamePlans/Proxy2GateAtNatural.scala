package Planning.Plans.Protoss.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.Parallel
import Planning.Plans.Macro.Automatic.{RequireSufficientPylons, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss

class Proxy2GateAtNatural extends Parallel {
  
  private def proxyZone: Option[Zone] = {
    With.geography.bases.find(base => base.isNaturalOf.isDefined && ! base.owner.isUs).map(_.zone)
  }
  
  children.set(Vector(
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon), zone = proxyZone),
        new Blueprint(this, building = Some(Protoss.Gateway), zone = proxyZone),
        new Blueprint(this, building = Some(Protoss.Gateway), zone = proxyZone),
        new Blueprint(this, building = Some(Protoss.Gateway), zone = proxyZone))
    },
    new Build(ProtossBuilds.Opening_TwoGate99_WithZealots: _*),
    new RequireSufficientPylons,
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Probe),
    new Attack))
}
package Planning.Plans.Protoss.GamePlans

import Macro.BuildRequests.{BuildRequest, RequestUnitAtLeast}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{ConsiderAttacking, DefendChokes}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.{RequireSufficientPylons, TrainGatewayUnitsContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsExactly
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss

class ProtossVsRandom extends Parallel {
  
  description.set("Protoss vs Random")
  
  private val safeBuild = Vector[BuildRequest] (
    RequestUnitAtLeast(3,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    RequestUnitAtLeast(1,   Protoss.Assimilator),
    RequestUnitAtLeast(4,   Protoss.Gateway)
  )
  
  children.set(Vector(
    new Build(ProtossBuilds.OpeningTwoGate1012: _*),
    new If(
      new UnitsExactly(0, UnitMatchType(Protoss.CyberneticsCore)),
      new Build(ProtossBuilds.OpeningTwoGate1012Zealots: _*)
    ),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new TrainGatewayUnitsContinuously,
    new Build(safeBuild: _*),
    new ScoutAt(7),
    new ConsiderAttacking,
    new DefendChokes
  ))
}
package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, RequestUpgradeLevel, _}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{ConsiderAttacking, ControlMap}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs Terran")
  
  private val lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(4,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(1,   Protoss.Forge),
    RequestUpgradeLevel(    Protoss.ZealotSpeed,      1),
    RequestUpgradeLevel(    Protoss.GroundDamage,     1),
    RequestUnitAtLeast(14,   Protoss.Gateway)
  )
  
  private class ConsiderTakingFourthBase extends IfThenElse(
    new UnitsAtLeast(20, UnitMatchWarriors),
    new RequireMiningBases(4)
  )
  
  children.set(Vector(
    new RequireMiningBases(1),
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateway_EarlyThird),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new ProtossVsTerranIdeas.RespondToBioAllInWithReavers,
    new MatchMiningBases(1),
    new ProtossVsTerranIdeas.RespondToBioWithReavers,
    new ConsiderTakingFourthBase,
    new TrainContinuously(Protoss.Reaver, 2),
    new ProtossVsTerranIdeas.BuildDragoonsUntilWeHaveZealotSpeed,
    new RequireMiningBases(2),
    new Build(RequestUpgradeLevel(Protoss.DragoonRange, 1)),
    new Build(RequestUnitAtLeast(2,   Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(RequestUnitAtLeast(3,   Protoss.Gateway)),
    new BuildAssimilators,
    new Build(lateGame),
    new ScoutAt(14),
    new ScoutExpansionsAt(100),
    new ControlMap,
    new ConsiderAttacking
  ))
}

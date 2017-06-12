package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic.Continuous.{RequireSufficientPylons, TrainContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{HaveUpgrade, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  private val _secondGateway = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Gateway)
  )
  
  private val _firstExpansion = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Nexus)
  )
  
  private val _lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(2,   Protoss.Nexus),
    RequestUnitAtLeast(2,   Protoss.Assimilator),
    RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
    RequestUnitAtLeast(1,   Protoss.RoboticsSupportBay),
    RequestUnitAtLeast(6,   Protoss.Gateway),
    RequestUnitAtLeast(3,   Protoss.Nexus),
    RequestUnitAtLeast(3,   Protoss.Assimilator),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUpgrade(         Protoss.ZealotSpeed),
    RequestUnitAtLeast(4,   Protoss.Nexus),
    RequestUnitAtLeast(4,   Protoss.Assimilator),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    RequestUnitAtLeast(5,   Protoss.Nexus),
    RequestUnitAtLeast(5,   Protoss.Assimilator),
    RequestUnitAtLeast(6,   Protoss.Nexus),
    RequestUnitAtLeast(6,   Protoss.Assimilator),
    RequestUnitAtLeast(7,   Protoss.Nexus),
    RequestUnitAtLeast(7,   Protoss.Assimilator)
  )
  
  children.set(Vector(
    new IfThenElse( //Emergency -- make sure we have some defense even if our economy is destroyed
      new And(
        new UnitsAtLeast(1, UnitMatchType(Protoss.Probe)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Assimilator)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Gateway)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore))
      ),
      new Parallel(
        new Build(RequestUnitAtLeast(1, Protoss.Probe)),
        new Build(RequestUnitAtLeast(2, Protoss.Dragoon))
      )
    ),
    new Build(ProtossBuilds.OpeningOneGateCore_DragoonFirst),
    new IfThenElse(
      new Or(
        new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))),
      new Parallel(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot)),
        new Build(ProtossBuilds.TakeNatural)
      )
    ),
    new IfThenElse(
      new And(
        new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
        new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver))
      ),
      new Build(ProtossBuilds.TakeThirdBase)
    ),
    new IfThenElse(
      new And(
        new UnitsAtLeast(15, UnitMatchType(Protoss.Dragoon)),
        new UnitsAtLeast(3, UnitMatchType(Protoss.Reaver))
      ),
      new Build(ProtossBuilds.TakeFourthBase)
    ),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new IfThenElse(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgrade(Protoss.ScarabDamage))
    ),
    new TrainContinuously(Protoss.Reaver, 4),
    new IfThenElse(
      new And(
        new HaveUpgrade(Protoss.ZealotSpeed),
        new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
      new TrainContinuously(Protoss.Zealot),
      new TrainContinuously(Protoss.Dragoon)
    ),
    new Build(_secondGateway),
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    new Build(ProtossBuilds.TechReavers),
    new Build(_lateGame),
    new ScoutExpansionsAt(70),
    new ScoutAt(9),
    new IfThenElse(
      new And(
        new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
        new HaveUpgrade(Protoss.DragoonRange)),
      new ConsiderAttacking
    )
  ))
}

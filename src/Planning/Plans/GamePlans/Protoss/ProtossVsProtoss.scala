package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{RequestUnitAtLeast, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound.{And, IfThenElse, Or, Parallel}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, HaveUpgrade, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  private val _secondGateway = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Gateway)
  )
  
  private val _lateGame = Vector[BuildRequest] (
    RequestUnitAtLeast(1,   Protoss.RoboticsFacility),
    RequestUnitAtLeast(1,   Protoss.RoboticsSupportBay),
    RequestUnitAtLeast(3,   Protoss.Gateway),
    RequestUnitAtLeast(2,   Protoss.Nexus),
    RequestUnitAtLeast(6,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUpgradeLevel(         Protoss.ZealotSpeed),
    RequestUnitAtLeast(10,  Protoss.Gateway)
  )
  
  private object MakeEmergencyUnits extends IfThenElse(
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
  )
  
  private object ExpandAgainstCannons extends IfThenElse(
    new Or(
      new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.PhotonCannon)),
      new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.Forge))
    ),
    new BuildMiningBases(2)
  )
  
  private object TakeNatural extends IfThenElse(
    new Or(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new Parallel(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot)),
      new BuildMiningBases(2)
    )
  )
  
  private object TakeThirdBase extends IfThenElse(
    new And(
      new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver))
    ),
    new BuildMiningBases(3)
  )
  
  private object TakeFourthBase extends IfThenElse(
    new And(
      new UnitsAtLeast(15, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(3, UnitMatchType(Protoss.Reaver))
    ),
    new BuildMiningBases(4)
  )
  
  private object UpgradeScarabDamage extends IfThenElse(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgradeLevel(Protoss.ScarabDamage))
  )
  
  private object BuildDragoonsorZealotsWithLegSpeed extends IfThenElse(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed),
      new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
  
  private object AttackWithDragoonRange extends IfThenElse(
    new And(
      new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
      new HaveUpgrade(Protoss.DragoonRange)),
    new ConsiderAttacking
  )
  
  children.set(Vector(
    MakeEmergencyUnits,
    new Build(ProtossBuilds.OpeningOneGateCore_DragoonFirst),
    new MatchMiningBases,
    TakeNatural,
    ExpandAgainstCannons,
    TakeThirdBase,
    TakeFourthBase,
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    UpgradeScarabDamage,
    new TrainContinuously(Protoss.Reaver, 4),
    BuildDragoonsorZealotsWithLegSpeed,
    new Build(_secondGateway),
    new Build(RequestUpgradeLevel(Protoss.DragoonRange)),
    new Build(ProtossBuilds.TechReavers),
    new Build(_lateGame),
    new ScoutExpansionsAt(70),
    new ScoutAt(9),
    AttackWithDragoonRange
  ))
}

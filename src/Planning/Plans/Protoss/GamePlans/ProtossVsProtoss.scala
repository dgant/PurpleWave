package Planning.Plans.Protoss.GamePlans

import Macro.BuildRequests.{RequestAtLeast, _}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{ConsiderAttacking, ControlMap}
import Planning.Plans.Compound.{And, If, Or, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildAssimilators, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, HaveUpgrade, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  private val _lateGame = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.RoboticsFacility),
    RequestAtLeast(1,   Protoss.RoboticsSupportBay),
    RequestAtLeast(3,   Protoss.Gateway),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(6,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(8,   Protoss.Gateway),
    RequestUpgrade(    Protoss.ZealotSpeed),
    RequestAtLeast(10,  Protoss.Gateway)
  )
  
  private class MakeEmergencyUnits extends If(
    new And(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Probe)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Assimilator)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Gateway)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore))
    ),
    new Parallel(
      new Build(RequestAtLeast(1, Protoss.Probe)),
      new Build(RequestAtLeast(2, Protoss.Dragoon))
    )
  ) { description.set("Make emergency units")}
  
  private class ExpandAgainstCannons extends If(
    new Or(
      new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.PhotonCannon)),
      new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.Forge))
    ),
    new RequireMiningBases(2)
  ) { description.set("Expand against cannons")}
  
  private class TakeNatural extends If(
    new Or(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new Parallel(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot)),
      new RequireMiningBases(2)
    )
  ) { description.set("Take our natural when safe")}
  
  private class TakeThirdBase extends If(
    new And(
      new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(3)
  ) { description.set("Take our third base when safe")}
  
  private class TakeFourthBase extends If(
    new And(
      new UnitsAtLeast(15, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(3, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(4)
  ) { description.set("Take our fourth base when safe")}
  
  private class UpgradeScarabDamage extends If(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgrade(Protoss.ScarabDamage))
  )  { description.set("Upgrade Scarab damage")}
  
  private class BuildDragoonsorZealotsWithLegSpeed extends If(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed, Protoss.Zealot.buildFrames),
      new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
  
  private class AttackWithDragoonRange extends If(
    new And(
      new UnitsAtLeast(8, UnitMatchType(Protoss.Dragoon)),
      new HaveUpgrade(Protoss.DragoonRange, 24 * 30)),
    new ConsiderAttacking
  )
  
  children.set(Vector(
    new MakeEmergencyUnits,
    new Build(ProtossBuilds.OpeningOneGateCore_DragoonFirst: _*),
    new MatchMiningBases,
    new TakeNatural,
    new ExpandAgainstCannons,
    new TakeThirdBase,
    new TakeFourthBase,
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    new UpgradeScarabDamage,
    new TrainContinuously(Protoss.Reaver, 4),
    new BuildDragoonsorZealotsWithLegSpeed,
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    new Build( RequestAtLeast(2, Protoss.Gateway)),
    new Build(ProtossBuilds.TechReavers: _*),
    new Build(_lateGame: _*),
    new ScoutExpansionsAt(70),
    new ScoutAt(9),
    new AttackWithDragoonRange,
    new ControlMap
  ))
}

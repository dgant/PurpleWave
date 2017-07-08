package Planning.Plans.Protoss.GamePlans

import Macro.BuildRequests.{RequestAtLeast, _}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.ControlMap
import Planning.Plans.Compound.{And, If, Or, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildAssimilators, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, HaveUpgrade, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  ///////////////////////////
  // Early game strategies //
  ///////////////////////////
  
  ////////////////////////
  // Midgame strategies //
  ////////////////////////
  
  ///////////////
  // Expanding //
  ///////////////
  
  private class ExpandAgainstCannons extends If(
    new Or(
      new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.PhotonCannon)),
      new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.Forge))),
    new RequireMiningBases(2)
  ) { description.set("Expand against cannons")}
  
  private class TakeNatural extends If(
    new Or(
      new UnitsAtLeast(8, UnitMatchWarriors),
      new UnitsAtLeast(2, UnitMatchType(Protoss.PhotonCannon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))),
    new RequireMiningBases(2)
  ) { description.set("Take our natural when safe")}
  
  private class TakeThirdBase extends If(
    new UnitsAtLeast(15, UnitMatchWarriors),
    new RequireMiningBases(3)
  ) { description.set("Take our third base when safe")}
  
  ///////////////
  // Late game //
  ///////////////
  
  private class UpgradeScarabDamage extends If(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgrade(Protoss.ScarabDamage))
  )  { description.set("Upgrade Scarab damage")}
  
  private class BuildDragoonsOrZealots extends If(
    new Or(
      new UnitsAtMost(0, UnitMatchType(Protoss.CyberneticsCore),  complete = true),
      new UnitsAtMost(0, UnitMatchType(Protoss.Assimilator),      complete = true),
      new And(
        new HaveUpgrade(Protoss.ZealotSpeed, Protoss.Zealot.buildFrames),
        new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon)))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon)
  )
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
    new Build(ProtossBuilds.Opening_1GateCore: _*),
    new MatchMiningBases,
    new TakeNatural,
    new ExpandAgainstCannons,
    new TakeThirdBase,
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    new UpgradeScarabDamage,
    new TrainContinuously(Protoss.Carrier, 4),
    new TrainContinuously(Protoss.Reaver, 4),
    new BuildDragoonsOrZealots,
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    new Build( RequestAtLeast(2, Protoss.Gateway)),
    new Build(ProtossBuilds.TechReavers: _*),
    new ScoutExpansionsAt(70),
    new ScoutAt(9),
    //TODO: Attack
    new ControlMap
  ))
}

package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, _}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{Attack, ConsiderAttacking, DefendZones}
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.{EnemyDarkTemplarExists, EnemyDarkTemplarPossible}
import Planning.Plans.Information.{Employ, Employing}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, BuildGasPumps, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{ForgeFastExpand, Nexus2GateThenCannons, TwoGatewaysAtNatural}
import Planning.Plans.Scouting.{Scout, ScoutExpansionsAt}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP._

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  ///////////////////////////
  // Early game strategies //
  ///////////////////////////
  
  private class ImplementEarly2Gate910 extends FirstEightMinutes(
    new Parallel(
      new TwoGatewaysAtNatural,
      new Build(ProtossBuilds.OpeningTwoGate910_WithZealots: _*)))
  
  private class ImplementEarly2Gate1012 extends FirstEightMinutes(
    new Parallel(
      new TwoGatewaysAtNatural,
      new Build(ProtossBuilds.OpeningTwoGate1012: _*)))
  
  private class ImplementEarly1GateCore extends FirstEightMinutes(
    new Build(ProtossBuilds.Opening_1GateCore: _*))
  
  private class ImplementEarly1GateZZCore extends FirstEightMinutes(
    new Build(ProtossBuilds.Opening_1GateZZCore: _*))
  
  private class ImplementEarlyFE extends FirstEightMinutes(
    new Parallel(
      new Nexus2GateThenCannons,
      new Build(
        RequestAtLeast(1,   Protoss.Nexus),
        RequestAtLeast(8,   Protoss.Probe),
        RequestAtLeast(1,   Protoss.Pylon),
        RequestAtLeast(12,  Protoss.Probe),
        RequestAtLeast(2,   Protoss.Nexus),
        RequestAtLeast(1,   Protoss.Gateway),
        RequestAtLeast(14,  Protoss.Probe),
        RequestAtLeast(2,   Protoss.Gateway),
        RequestAtLeast(15,  Protoss.Probe)),
      new TrainContinuously(Protoss.Zealot),
      new Build(
        RequestAtLeast(2, Protoss.Pylon),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(3, Protoss.PhotonCannon)
      )))
    
  private class ImplementEarlyFFE extends FirstEightMinutes(
    new Parallel(
      new ForgeFastExpand,
      new Build(ProtossBuilds.FFE_ForgeFirst: _*)))
  
  ////////////////////////
  // Midgame strategies //
  ////////////////////////
  
  private class ImplementMidgame4GateGoon extends
    Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgameDarkTemplar extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives)))
  
  private class ImplementMidgameFE extends If(
    new UnitsAtLeast(3, UnitMatchWarriors, complete = false),
    new RequireMiningBases(2))
  
  private class ImplementMidgameReaver extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)))
  
  private class ImplementMidgameObserverReaver extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)))
  
  private class BuildReaversOrTemplar extends If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.TemplarArchives), complete = true),
      new Parallel(
        new TrainContinuously(Protoss.HighTemplar,  6),
        new TrainContinuously(Protoss.Reaver,       1)),
      new TrainContinuously(Protoss.Reaver, 4))
  
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
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver)),
      new UnitsAtLeast(2, UnitMatchType(Protoss.DarkTemplar))),
    new RequireMiningBases(2)
  ) { description.set("Take our natural when safe")}
  
  private class TakeThirdBase extends If(
    new UnitsAtLeast(15, UnitMatchWarriors),
    new RequireMiningBases(3)
  ) { description.set("Take our third base when safe")}
  
  ///////////////
  // Reactions //
  ///////////////
  
  private class ReactToDarkTemplarPossible extends If(
    new EnemyDarkTemplarPossible,
    new Parallel(
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory)),
      new TrainContinuously(Protoss.Observer, 2)))
  
  private class ReactToDarkTemplarExisting extends If(
    new EnemyDarkTemplarExists,
    new Parallel(
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(1, Protoss.PhotonCannon)),
      new TrainContinuously(Protoss.Observer, 3)))
  
  ///////////////
  // Late game //
  ///////////////
  
  private class BuildDragoonsOrZealots extends If(
    new Or(
      new UnitsAtMost(0, UnitMatchType(Protoss.CyberneticsCore),  complete = true),
      new UnitsAtMost(0, UnitMatchType(Protoss.Assimilator),      complete = true),
      new Check(() => With.self.gas < 30),
      new Check(() => With.self.gas < 100 && With.self.minerals > With.self.gas * 5),
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
  
    new MeldArchons(40),
    
    // Early game
    new RequireMiningBases(1),
    new Employ(PvPEarly1GateCore,       new ImplementEarly1GateCore),
    new Employ(PvPEarly1GateZZCore,     new ImplementEarly1GateZZCore),
    new Employ(PvPEarly2Gate910,        new ImplementEarly2Gate910),
    new Employ(PvPEarly2Gate1012,       new ImplementEarly2Gate1012),
    new Employ(PvPEarlyFE,              new ImplementEarlyFE),
    new Employ(PvPEarlyFFE,             new ImplementEarlyFFE),
    
    // Expanding
    new MatchMiningBases,
    new TakeNatural,
    new ExpandAgainstCannons,
    new TakeThirdBase,
  
    // Early game macro
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    
    // Units/Upgrades
    
    new ReactToDarkTemplarExisting,
    new ReactToDarkTemplarPossible,
    
    new If(
      new UnitsAtLeast(2, UnitMatchType(Protoss.HighTemplar), complete = false),
      new Build(RequestTech(Protoss.PsionicStorm))),
  
    new If(
      new And(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Dragoon), complete = false),
        new Or(
          new Not(new Employing(PvPMidgameDarkTemplar)),
          new UnitsAtLeast(2, UnitMatchType(Protoss.DarkTemplar), complete = false))),
      new Build(RequestUpgrade(Protoss.DragoonRange))),
    
    new If(
      new And(
        new EnemyUnitsAtMost(0, UnitMatchType(Protoss.Observer)),
        new EnemyUnitsAtMost(0, UnitMatchType(Protoss.Forge))),
      new TrainContinuously(Protoss.DarkTemplar, 3),
      new TrainContinuously(Protoss.DarkTemplar, 1)),
    
    new If(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgrade(Protoss.ScarabDamage))),
    
    new OnMiningBases(2, new UpgradeContinuously(Protoss.ZealotSpeed)),
    
    new TrainMatchingRatio(Protoss.Observer, 1, 2, Seq(MatchingRatio(UnitMatchType(Protoss.DarkTemplar), 2.0))),
    new TrainContinuously(Protoss.Reaver, 4),
    new BuildDragoonsOrZealots,
    
    // Midgame
    
    // Don't directly go expand-> tech
    new OnMiningBases(2,
      new If(
        new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore)),
        new Build(RequestAtLeast(4, Protoss.Gateway)))),
      
    new Employ(PvPMidgame4GateGoon,       new ImplementMidgame4GateGoon),
    new Employ(PvPMidgameDarkTemplar,     new ImplementMidgameDarkTemplar),
    new Employ(PvPMidgameObserverReaver,  new ImplementMidgameObserverReaver),
    new Employ(PvPMidgameReaver,          new ImplementMidgameReaver),
    
    // Default builds
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new Parallel(
      new Build(
        RequestAtLeast(2, Protoss.Gateway),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(3, Protoss.Gateway),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.RoboticsSupportBay)),
      new OnMiningBases(2,
        new Build(
          RequestAtLeast(5, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(8, Protoss.Gateway))),
      new OnMiningBases(3,
        new Parallel(
          new UpgradeContinuously(Protoss.GroundDamage),
          new Build(
            RequestAtLeast(1, Protoss.Forge),
            RequestAtLeast(1, Protoss.TemplarArchives),
            RequestAtLeast(2, Protoss.Forge),
            RequestUpgrade(Protoss.HighTemplarEnergy)),
          new UpgradeContinuously(Protoss.GroundArmor),
          new Build(RequestAtLeast(12, Protoss.Gateway))))),
    
    new ScoutExpansionsAt(100),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
      new Scout),
  
    new DefendZones,
    new Attack { attackers.get.unitMatcher.set(UnitMatchType(Protoss.DarkTemplar)) },
    new If(
      new And(
        new Employing(PvPMidgameDarkTemplar),
        new Not(new Employing(PvPEarly2Gate910)),
        new Not(new Employing(PvPEarly2Gate1012))),
      new Trigger(
        new UnitsAtLeast(1, UnitMatchType(Protoss.DarkTemplar), complete = true),
        new ConsiderAttacking),
      new ConsiderAttacking)
  ))
}

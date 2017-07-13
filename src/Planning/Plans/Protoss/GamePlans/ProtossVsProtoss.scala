package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, _}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{Attack, ConsiderAttacking, ControlMap}
import Planning.Plans.Compound._
import Planning.Plans.Information.{Employ, Employing}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstFiveMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{ForgeFastExpand, Nexus2GateThenCannons, TwoGatewaysAtNatural}
import Planning.Plans.Scouting.{RequireEnemyBase, ScoutExpansionsAt}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Options.Protoss.PvP._

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  ///////////////////////////
  // Early game strategies //
  ///////////////////////////
  
  private class ImplementEarly2Gate910 extends FirstFiveMinutes(
    new Parallel(
      new TwoGatewaysAtNatural,
      new Build(ProtossBuilds.OpeningTwoGate910_WithZealots: _*)))
  
  private class ImplementEarly2Gate1012 extends FirstFiveMinutes(
    new Parallel(
      new TwoGatewaysAtNatural,
      new Build(ProtossBuilds.OpeningTwoGate1012: _*)))
  
  private class ImplementEarly1GateCore extends FirstFiveMinutes(
    new Build(ProtossBuilds.Opening_1GateCore: _*))
  
  private class ImplementEarly1GateZZCore extends FirstFiveMinutes(
    new Build(ProtossBuilds.Opening_1GateZZCore: _*))
  
  private class ImplementEarlyFE extends FirstFiveMinutes(
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
        RequestAtLeast(3, Protoss.PhotonCannon))))
    
  private class ImplementEarlyFFE extends FirstFiveMinutes(
    new Parallel(
      new ForgeFastExpand(cannonsInFront = false),
      new Build(ProtossBuilds.FFE_ForgeFirst: _*)))
  
  ////////////////////////
  // Midgame strategies //
  ////////////////////////
  
  private class ImplementMidgame4GateGoon extends
    Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgameCarriers extends Parallel(
    new OnMiningBases(2,
      new If(
        new UnitsAtLeast(20, UnitMatchWarriors),
        new Build(
          RequestAtLeast(1, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.Stargate)),
        new If(
          new UnitsAtLeast(1, UnitMatchType(Protoss.Stargate), complete = true),
          new Build(
            RequestAtLeast(1, Protoss.FleetBeacon),
            RequestAtLeast(2, Protoss.Stargate),
            RequestUpgrade(Protoss.AirDamage),
            RequestUpgrade(Protoss.CarrierCapacity))))))
  
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
  // Late game //
  ///////////////
  
  private class BuildDragoonsOrZealots extends If(
    new Or(
      new And(
        new Employing(PvPMidgameCarriers),
        new UnitsAtLeast(1, UnitMatchType(Protoss.FleetBeacon), complete = false)),
      new UnitsAtMost(0, UnitMatchType(Protoss.CyberneticsCore),  complete = true),
      new UnitsAtMost(0, UnitMatchType(Protoss.Assimilator),      complete = true),
      new Check(() => With.self.gas < 30),
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
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    
    // Units/Upgrades
  
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
      new TrainContinuously(Protoss.DarkTemplar, 3)),
    
    new If(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver)),
      new Build(RequestUpgrade(Protoss.ScarabDamage))),
    
    new OnMiningBases(2, new Build(RequestUpgrade(Protoss.ZealotSpeed))),
    
    new TrainContinuously(Protoss.Carrier),
    new If(
      new EnemyHasShown(Protoss.DarkTemplar),
      new Parallel(
        new TrainContinuously(Protoss.Observer, 2),
        new TrainContinuously(Protoss.Reaver, 4)),
      new Parallel(
        new TrainContinuously(Protoss.Reaver, 4),
        new TrainContinuously(Protoss.Observer, 1))),
    new BuildDragoonsOrZealots,
    
    // Midgame
    new Employ(PvPMidgame4GateGoon,       new ImplementMidgame4GateGoon),
    new Employ(PvPMidgameDarkTemplar,     new ImplementMidgameDarkTemplar),
    new Employ(PvPMidgameCarriers,        new ImplementMidgameCarriers),
    new Employ(PvPMidgameObserverReaver,  new ImplementMidgameObserverReaver),
    new Employ(PvPMidgameReaver,          new ImplementMidgameReaver),
    
    // Default builds
    new Build(RequestAtLeast(1, Protoss.Gateway)),
    new BuildAssimilators,
    new If (
      new Employing(PvPMidgameCarriers),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestUpgrade(Protoss.DragoonRange),
          RequestAtLeast(3, Protoss.Gateway)),
        new RequireMiningBases(2),
        new OnMiningBases(2,
          new Parallel(
            new If(
              new And(
                new MiningBasesAtLeast(2),
                new UnitsAtLeast(1, UnitMatchType(Protoss.Forge), complete = true)),
              new Build(
                RequestAtLeast(1, Protoss.Forge),
                RequestAtLeast(6, Protoss.PhotonCannon))),
            new Build(
              RequestAtLeast(1, Protoss.CyberneticsCore),
              RequestAtLeast(1, Protoss.Stargate))))),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestUpgrade(Protoss.DragoonRange),
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay)),
        new OnMiningBases(2,
          new Build(
            RequestAtLeast(5, Protoss.Gateway),
            RequestAtLeast(1, Protoss.Observatory),
            RequestAtLeast(1, Protoss.CitadelOfAdun))),
        new OnMiningBases(3,
          new Build(
            RequestAtLeast(2, Protoss.RoboticsFacility),
            RequestAtLeast(10, Protoss.Gateway))))),
    
    new ScoutExpansionsAt(70),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
      new RequireEnemyBase),
  
    new Attack { attackers.get.unitMatcher.set(UnitMatchType(Protoss.DarkTemplar)) },
    new If(
      new And(
        new Employing(PvPMidgameDarkTemplar),
        new Not(new Employing(PvPEarly2Gate910)),
        new Not(new Employing(PvPEarly2Gate1012))),
      new Trigger(
        new UnitsAtLeast(1, UnitMatchType(Protoss.DarkTemplar), complete = true),
        new ConsiderAttacking),
      new If (
        new Employing(PvPMidgameCarriers),
        new If(
          new UnitsAtLeast(6 * 8, UnitMatchType(Protoss.Interceptor), complete = true),
          new ConsiderAttacking),
        new ConsiderAttacking)),
    
    new ControlMap
  ))
}

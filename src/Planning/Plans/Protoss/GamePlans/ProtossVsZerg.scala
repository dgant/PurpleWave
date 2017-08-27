package Planning.Plans.Protoss.GamePlans

import Information.StrategyDetection.ZergStrategies._
import Lifecycle.With
import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Reactive.EnemyMutalisks
import Planning.Plans.Information.Scenarios.EnemyStrategy
import Planning.Plans.Information.{Employ, Employing, StartPositionsAtLeast}
import Planning.Plans.Macro.Automatic.{MatchingRatio, _}
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational._
import Planning.Plans.Scouting.{FindExpansions, Scout, ScoutAt}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZ._

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs. Zerg")
  
  ////////////////
  // Early game //
  ////////////////
  
  private class WeAreFFEing extends Or(
    new Employing(PvZEarlyFFEEconomic),
    new Employing(PvZEarlyFFEConservative),
    new Employing(PvZEarlyFFEGatewayFirst),
    new Employing(PvZEarlyFFENexusFirst))
  
  private class ImplementEarly2Gate extends FirstEightMinutes(
    new Parallel(
      new TwoGatewaysAtNexus,
      new Trigger(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
        initialBefore = new Build(ProtossBuilds.OpeningTwoGate1012: _*))))
  
  private class FFE extends FirstEightMinutes(
    new Parallel(
      new ForgeFastExpand,
      new If(
        new EnemyStrategy(new Fingerprint4Pool),
        new Build(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Or(
          new EnemyStrategy(new Fingerprint9Pool),
          new EnemyStrategy(new FingerprintOverpool),
          new EnemyStrategy(new Fingerprint10Hatch9Pool)),
        new Build(ProtossBuilds.FFE_ForgeFirst: _*),
      new If(
        new EnemyStrategy(new Fingerprint12Hatch),
        new If(
          new Employing(PvZEarlyFFEGatewayFirst),
          new Build(ProtossBuilds.FFE_GatewayFirst: _*),
          new Build(ProtossBuilds.FFE_NexusFirst: _*)),
      new If(
        new Employing(PvZEarlyFFEConservative),
        new Build(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Employing(PvZEarlyFFEGatewayFirst),
        new Build(ProtossBuilds.FFE_GatewayFirst: _*),
      new If(
        new Employing(PvZEarlyFFENexusFirst),
        new Build(ProtossBuilds.FFE_NexusFirst: _*),
        new Build(ProtossBuilds.FFE_ForgeFirst: _*))))))),
      new RequireMiningBases(2),
      new If(
        new EnemyStrategy(new Fingerprint10Hatch9Pool),
        new Build(RequestAtLeast(4, Protoss.PhotonCannon))),
      new FFEFollowUp
    ))
  
  private class FFEFollowUp extends Build(
    RequestAtLeast(1, Protoss.Forge),
    RequestAtLeast(2, Protoss.PhotonCannon),
    RequestAtLeast(2, Protoss.Nexus),
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(3, Protoss.PhotonCannon))
  
  private class TwoGateFollowUp extends Build(
    RequestAtLeast(1, Protoss.Assimilator),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(2, Protoss.Nexus),
    RequestAtLeast(3, Protoss.Gateway),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(4, Protoss.Gateway))
  
  /////////////
  // Midgame //
  /////////////
  
  private class ImplementMidgame5GateDragoons extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Zealot),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway))
  
  private class ImplementMidgameCorsairSpeedlot extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.GroundDamage),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(5, Protoss.Gateway))
  
  private class ImplementMidgameCorsairReaver extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(6, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Stargate),
      RequestAtLeast(1, Protoss.RoboticsFacility))
  
  private class ImplementMidgameCorsairDarkTemplar extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway))
  
  ///////////
  // Macro //
  ///////////
  
  private class TakeSafeNatural extends If(
    new UnitsAtLeast(6, UnitMatchWarriors),
    new RequireMiningBases(2))
  
  private class TakeSafeThirdBase extends If(
    new UnitsAtLeast(16, UnitMatchWarriors),
    new RequireMiningBases(3))
  
  private class BuildDetectionForLurkers extends If(
    new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Lurker)),
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Observer)))
  
  private class CanBuildDragoons extends Check(() => With.self.minerals < 1000 || With.self.gas > 200)
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
    
    new Aggression(0.7),
    
    new If(
      new HaveTech(Protoss.PsionicStorm),
      new MeldArchons(40),
      new MeldArchons),
    
    /////////////////////////////
    // Early game build orders //
    /////////////////////////////
  
    new RequireBareMinimum,
    new Employ(PvZEarly2Gate, new ImplementEarly2Gate),
    new If(new WeAreFFEing, new FFE),
    
    ///////////////////
    // Early defense //
    ///////////////////
    
    new FirstEightMinutes(
      new If(
        new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore), complete = false),
        new Parallel(
          new TrainMatchingRatio(Protoss.PhotonCannon, 2, 6,
            Seq(
              MatchingRatio(UnitMatchType(Zerg.Zergling), 0.3),
              MatchingRatio(UnitMatchType(Zerg.Hydralisk), 0.5)))))),
  
    new FirstEightMinutes(
      new If(
        new And(
          new WeAreFFEing,
          new EnemyStrategy(new Fingerprint4Pool),
          new Check(() => With.frame > 24 * 125),
          new UnitsAtMost(1, UnitMatchType(Protoss.PhotonCannon), complete = true)),
        new DefendFFEWithProbesAgainst4Pool)),
  
    new FirstEightMinutes(
      new If(
        new And(
          new WeAreFFEing,
          new Or(
            new EnemyStrategy(new Fingerprint9Pool),
            new EnemyStrategy(new FingerprintOverpool)),
          new Check(() => With.frame > 24 * 125),
          new UnitsAtMost(2, UnitMatchType(Protoss.PhotonCannon), complete = true)),
        new DefendFFEWithProbesAgainst9Pool)),
    
    new FirstEightMinutes(new Defend2GateAgainst4Pool),
  
    /////////////////
    // Early macro //
    /////////////////
    
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new BuildDetectionForLurkers,
    new TakeSafeNatural,
    new TakeSafeThirdBase,
    new BuildCannonsAtExpansions(5),
  
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Dragoon), complete = false),
      new Build(RequestUpgrade(Protoss.DragoonRange))),
  
    new If(
      new UnitsAtLeast(2, UnitMatchType(Protoss.HighTemplar), complete = false),
      new Build(RequestTech(Protoss.PsionicStorm))),
  
    new If(
      new UnitsAtLeast(3, UnitMatchType(Protoss.Reaver), complete = false),
      new Build(RequestUpgrade(Protoss.ShuttleSpeed))),
    
    new If(
      new And(
        new UnitsAtLeast(2, UnitMatchWarriors, complete = false),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Forge), complete = true),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Assimilator), complete = true)),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.ZealotSpeed))),
  
    new If(
      new UnitsAtLeast(3, UnitMatchType(Protoss.Corsair), complete = false),
      new Build(RequestUpgrade(Protoss.AirDamage, 1))),
  
    new If(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Corsair), complete = false),
      new Build(RequestUpgrade(Protoss.AirArmor, 1))),
  
    new If(
      new And(
        new Employing(PvZMidgameCorsairDarkTemplar),
        new UnitsAtLeast(2, UnitMatchType(Protoss.DarkTemplar), complete = true)),
      new TrainMatchingRatio(Protoss.Corsair, 5, Int.MaxValue, Seq(MatchingRatio(UnitMatchType(Zerg.Mutalisk), 1.5))),
      new If(
        new And(
          new Employing(PvZMidgameCorsairReaver),
          new UnitsAtLeast(3, UnitMatchType(Protoss.Corsair), complete = true)),
        new TrainMatchingRatio(Protoss.Corsair, 5, Int.MaxValue, Seq(MatchingRatio(UnitMatchType(Zerg.Mutalisk), 1.5))),
        new TrainMatchingRatio(Protoss.Corsair, 1, Int.MaxValue, Seq(MatchingRatio(UnitMatchType(Zerg.Mutalisk), 1.5))))),
  
    new If(
      new EnemyUnitsAtLeast(4, UnitMatchType(Zerg.Mutalisk)),
      new Build(RequestAtLeast(2, Protoss.Stargate))),
    new OnGasBases(2,
      new If(
        new EnemyUnitsAtLeast(13, UnitMatchType(Zerg.Mutalisk)),
        new Build(RequestAtLeast(3, Protoss.Stargate)))),
    
    new TrainMatchingRatio(Protoss.Observer, 0, 3, Seq(MatchingRatio(UnitMatchType(Zerg.Lurker), 0.5))),
  
    // Gateway production
    new If(
      
      // Emergency Dragoons
      new And(
        new EnemyMutalisks,
        new UnitsAtMost(5, UnitMatchType(Protoss.Corsair))),
      new If(
        new CanBuildDragoons,
        new TrainContinuously(Protoss.Dragoon),
        new TrainContinuously(Protoss.Zealot)),
      
      // Normal behavior
      new Parallel(
        new If(
          new Employing(PvZMidgameCorsairDarkTemplar),
          new TrainContinuously(Protoss.DarkTemplar, 3),
          new TrainContinuously(Protoss.DarkTemplar, 1)),
        new If(
          new And(
            new UnitsAtLeast(3, Protoss.Reaver, complete = false),
            new UnitsAtMost(0, Protoss.Shuttle, complete = false)),
          new TrainContinuously(Protoss.Shuttle, 1),
          new TrainContinuously(Protoss.Reaver, 6)),
        new If(
          new And(
            new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true),
            new Not(new Employing(PvZMidgameCorsairReaver))),
          new Build(RequestAnother(2, Protoss.HighTemplar))),
        new If(
          new And(
            new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore), complete = true),
            new Or(
              new And(
                new Employing(PvZMidgame5GateDragoons),
                new UnitsAtMost(15, UnitMatchType(Protoss.Dragoon))),
              new UnitsAtLeast(10, UnitMatchType(Protoss.Zealot)))),
          new TrainContinuously(Protoss.Dragoon),
          new TrainContinuously(Protoss.Zealot)))),
  
    new Employ(PvZEarly2Gate,                   new TwoGateFollowUp),
    new Employ(PvZMidgame5GateDragoons,         new ImplementMidgame5GateDragoons),
    new Employ(PvZMidgameCorsairDarkTemplar,    new ImplementMidgameCorsairDarkTemplar),
    new Employ(PvZMidgameCorsairReaver,         new ImplementMidgameCorsairReaver),
    new Employ(PvZMidgameCorsairSpeedlot,       new ImplementMidgameCorsairSpeedlot),
    new BuildGasPumps,
  
    /////////////////////
    // Late game macro //
    /////////////////////
    
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed)),
      
    new If(
      new Not(new Employing(PvZMidgameCorsairReaver)),
      new Build(
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestUpgrade(Protoss.GroundDamage, 2),
        RequestUpgrade(Protoss.HighTemplarEnergy),
        RequestTech(Protoss.PsionicStorm),
        RequestUpgrade(Protoss.GroundDamage, 3))),
      
    new Build(
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(6, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(3, Protoss.Nexus),
      RequestAtLeast(12, Protoss.Gateway),
      RequestAtLeast(4, Protoss.Nexus)),
  
    new UpgradeContinuously(Protoss.GroundArmor),
  
    /////////////
    // Tactics //
    /////////////
    
    new If(
      new WeAreFFEing,
      new If(
        new And(
          new Not(new Employing(PvZEarlyFFEConservative)),
          new Check(() => With.units.ours.exists(u => u.is(Protoss.Pylon) && With.framesSince(u.frameDiscovered) > 24))),
        new If(
          new StartPositionsAtLeast(4),
          new Scout(2),
          new Scout(1))),
      new ScoutAt(14)),
    
    new If(
      new And(
        new Or(
          new UnitsAtMost(0, UnitMatchType(Protoss.DarkTemplar), complete = false),
          new Not(new Employing(PvZMidgameCorsairDarkTemplar))),
        new EnemyUnitsAtMost(0, UnitMatchType(Zerg.Spire), complete = true),
        new EnemyUnitsAtMost(0, UnitMatchType(Zerg.Mutalisk)),
        new EnemyUnitsAtMost(0, UnitMatchType(Zerg.Scourge))),
      new Parallel(
        new FindExpansions       { scouts.get.unitMatcher.set(UnitMatchType(Protoss.Corsair)) },
        new ControlEnemyAirspace { flyers.get.unitMatcher.set(UnitMatchType(Protoss.Corsair)) })),
  
    new ClearBurrowedBlockers,
    new FindExpansions { scouts.get.unitMatcher.set(UnitMatchType(Protoss.DarkTemplar)) },
    new DefendZones,
    new If(
      new HaveUpgrade(Protoss.ShuttleSpeed),
      new DropAttack),
    new If(
      new UnitsAtLeast(4, UnitMatchWarriors, complete = true),
      new ConsiderAttacking)
  ))
}

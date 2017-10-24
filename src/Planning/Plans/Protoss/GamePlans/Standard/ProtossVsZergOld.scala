package Planning.Plans.Protoss.GamePlans.Standard

import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Reactive.EnemyMutalisks
import Planning.Plans.Information.Scenarios.EnemyStrategy
import Planning.Plans.Information.{Employ, Employing, SafeAtHome, StartPositionsAtLeast}
import Planning.Plans.Macro.Automatic.{MatchingRatio, _}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational._
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.{FindExpansions, Scout, ScoutAt}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZ._

class ProtossVsZergOld extends Parallel {
  
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
      new BuildHuggingNexus,
      new BuildOrder(ProtossBuilds.OpeningTwoGate1012: _*))))
  
  private class FFE extends FirstEightMinutes(
    new Parallel(
      new ForgeFastExpand,
      new If(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
        new BuildOrder(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Or(
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint9Pool),
          new EnemyStrategy(With.intelligence.fingerprints.fingerprintOverpool),
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint10Hatch9Pool)),
        new BuildOrder(ProtossBuilds.FFE_ForgeFirst: _*),
      new If(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint12Hatch),
        new If(
          new Employing(PvZEarlyFFEGatewayFirst),
          new BuildOrder(ProtossBuilds.FFE_GatewayFirst_Aggressive: _*), //Note -- BuildOrder, not Build! So we can train but not replace Zealots
          new BuildOrder(ProtossBuilds.FFE_NexusFirst: _*)),
      new If(
        new Employing(PvZEarlyFFEConservative),
        new BuildOrder(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Employing(PvZEarlyFFEGatewayFirst),
        new BuildOrder(ProtossBuilds.FFE_GatewayFirst_Aggressive: _*), //Note -- BuildOrder, not Build! So we can train but not replace Zealots
      new If(
        new Employing(PvZEarlyFFENexusFirst),
        new BuildOrder(ProtossBuilds.FFE_NexusFirst: _*),
        new BuildOrder(ProtossBuilds.FFE_ForgeFirst: _*))))))),
      new RequireMiningBases(2),
      new If(
        new EnemyStrategy(With.intelligence.fingerprints.fingerprint10Hatch9Pool),
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
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgameCorsairReaver extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(2, Protoss.Nexus),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(4, Protoss.Gateway),
      RequestAtLeast(1, Protoss.TemplarArchives))
  
  private class ImplementMidgameCorsairDarkTemplar extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgame2Stargate extends Build(
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.Assimilator),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(2, Protoss.Assimilator),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(1, Protoss.CitadelOfAdun),
    RequestAtLeast(2, Protoss.Nexus),
    RequestAtLeast(2, Protoss.Stargate),
    RequestAtLeast(4, Protoss.Gateway))
  
  ///////////
  // Macro //
  ///////////
  
  private class TakeSafeNatural extends If(
    new Or(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(12, UnitMatchWarriors)),
      new UnitsAtLeast(6, UnitMatchWarriors)),
    new RequireMiningBases(2))
  
  private class TakeSafeThirdBase extends If(
    new Or(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(15, UnitMatchWarriors)),
      new UnitsAtLeast(24, UnitMatchWarriors)),
    new RequireMiningBases(3))
  
  private class BuildDetectionForLurkers extends If(
    new Or(
      new EnemyHasShown(Zerg.Lurker),
      new EnemyHasShown(Zerg.LurkerEgg),
      new And(
        new SafeAtHome,
        new EnemyHasShown(Zerg.Hydralisk),
        new EnemyHasShown(Zerg.Lair))),
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Observer)))
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
    
    new Aggression(0.75),
    
    new If(
      new TechComplete(Protoss.PsionicStorm),
      new MeldArchons(40),
      new MeldArchons),
    
    /////////////////////////////
    // Early game build orders //
    /////////////////////////////
  
    new RequireEssentials,
    new Employ(PvZEarly2Gate, new ImplementEarly2Gate),
    new If(new WeAreFFEing, new FFE),
    
    ///////////////////
    // Early defense //
    ///////////////////
    
    new FirstEightMinutes(
      new If(
        new UnitsAtMost(2, Protoss.Gateway, complete = true),
        new Parallel(
          new TrainMatchingRatio(Protoss.PhotonCannon, 2, 6,
            Seq(
              MatchingRatio(Zerg.Zergling, 0.3),
              MatchingRatio(Zerg.Hydralisk, 0.75)))))),
  
    new FirstEightMinutes(
      new If(
        new And(
          new WeAreFFEing,
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
          new Check(() => With.frame > 24 * 125),
          new UnitsAtLeast(1, Protoss.PhotonCannon, complete = false),
          new UnitsAtMost(1, Protoss.PhotonCannon, complete = true)),
        new DefendFFEWithProbesAgainst4Pool)),
  
    new FirstEightMinutes(
      new If(
        new And(
          new EnemyUnitsAtLeast(4, Zerg.Zergling),
          new WeAreFFEing,
          new Or(
            new EnemyStrategy(With.intelligence.fingerprints.fingerprint9Pool),
            new EnemyStrategy(With.intelligence.fingerprints.fingerprintOverpool)),
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint10Hatch9Pool),
          new Check(() => With.frame > 24 * 125),
          new UnitsAtMost(2, Protoss.PhotonCannon, complete = true)),
        new DefendFFEWithProbesAgainst9Pool)),
    
    new FirstEightMinutes(new DefendZealotsAgainst4Pool),
  
    /////////////////
    // Early macro //
    /////////////////
    
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildDetectionForLurkers,
    new TakeSafeNatural,
    new TakeSafeThirdBase,
    new BuildCannonsAtExpansions(5),
  
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.Carrier),      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(4, Protoss.Corsair),      new UpgradeContinuously(Protoss.AirDamage)),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new If(new EnemyUnitsAtMost(0, Zerg.Scourge), new Build(RequestUpgrade(Protoss.ShuttleSpeed)))),
    new If(
      new And(
        new UnitsAtLeast(2, Protoss.Zealot),
        new UnitsAtLeast(1, Protoss.Assimilator, complete = true)),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.ZealotSpeed))),
  
    new If(new UnitsAtLeast(6, Protoss.Corsair), new TrainContinuously(Protoss.Carrier)),
    new If(new UnitsAtLeast(6, Protoss.Corsair), new UpgradeContinuously(Protoss.AirArmor)),
    new If(new UnitsAtLeast(7, Protoss.Corsair), new Build(RequestAtLeast(1, Protoss.FleetBeacon))),
    new If(new UnitsAtLeast(8, Protoss.Corsair), new If(new UnitsAtLeast(1, Protoss.FleetBeacon), new Build(RequestTech(Protoss.DisruptionWeb)))),
    
    new If(
      new And(
        new Employing(PvZMidgameCorsairDarkTemplar),
        new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true)),
      new TrainMatchingRatio(Protoss.Corsair, 5, Int.MaxValue, Seq(MatchingRatio(Zerg.Mutalisk, 1.5))),
      new If(
        new Or(
          new Employing(PvZMidgame2Stargate),
          new Employing(PvZMidgameCorsairReaver)),
        new TrainMatchingRatio(Protoss.Corsair, 8, Int.MaxValue, Seq(MatchingRatio(Zerg.Mutalisk, 1.5))),
        new TrainMatchingRatio(Protoss.Corsair, 1, Int.MaxValue, Seq(MatchingRatio(Zerg.Mutalisk, 1.5))))),
  
    new OnGasBases(2,
      new If(
        new EnemyUnitsAtLeast(13, Zerg.Mutalisk),
        new Build(RequestAtLeast(3, Protoss.Stargate)),
        new If(
          new EnemyUnitsAtLeast(7, Zerg.Mutalisk),
          new Build(RequestAtLeast(2, Protoss.Stargate))))),
    
    new TrainMatchingRatio(Protoss.Observer, 0, 3, Seq(MatchingRatio(Zerg.Lurker, 0.5))),
    
    // Gateway production
    new If(
      
      // Emergency Dragoons
      new And(
        new EnemyMutalisks,
        new UnitsAtMost(5, Protoss.Corsair),
        new UnitsAtMost(12, Protoss.Dragoon)),
      new Parallel(
        new BuildCannonsAtBases(2, PlacementProfiles.cannonAgainstAir),
        new TrainMatchingRatio(Protoss.Dragoon, 0, 6, Seq(MatchingRatio(Zerg.Mutalisk, 0.75)))),
      
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
            new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
            new Or(
              new Employing(PvZMidgame5GateDragoons),
              new Employing(PvZMidgameCorsairReaver)),
            new Or(
              new UnitsAtMost(12, Protoss.Dragoon),
              new UnitsAtLeast(12, Protoss.Zealot))),
          new TrainContinuously(Protoss.Dragoon),
          new TrainContinuously(Protoss.Zealot)))),
  
    new Employ(PvZEarly2Gate,                   new TwoGateFollowUp),
    new Employ(PvZMidgame5GateDragoons,         new ImplementMidgame5GateDragoons),
    new Employ(PvZMidgameCorsairDarkTemplar,    new ImplementMidgameCorsairDarkTemplar),
    new Employ(PvZMidgameCorsairReaver,         new ImplementMidgameCorsairReaver),
    new Employ(PvZMidgameCorsairSpeedlot,       new ImplementMidgameCorsairSpeedlot),
    new Employ(PvZMidgame2Stargate,             new ImplementMidgame2Stargate),
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
      new And(
        new Not(new Employing(PvZMidgameCorsairReaver)),
        new Not(new Employing(PvZMidgame2Stargate))),
      new Build(
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestUpgrade(Protoss.GroundDamage, 2),
        RequestAtLeast(8, Protoss.Gateway),
        RequestTech(Protoss.PsionicStorm),
        RequestUpgrade(Protoss.GroundDamage, 3),
        RequestUpgrade(Protoss.HighTemplarEnergy))),
      
    new Build(
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(6, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(3, Protoss.Nexus),
      RequestAtLeast(12, Protoss.Gateway),
      RequestAtLeast(4, Protoss.Nexus),
      RequestAtLeast(15, Protoss.Gateway)),
  
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
        new EnemyUnitsAtMost(0, Zerg.Spire, complete = true),
        new EnemyUnitsAtMost(0, Zerg.Mutalisk),
        new EnemyUnitsAtMost(0, Zerg.Scourge)),
      new Parallel(
        new FindExpansions { scouts.get.unitMatcher.set(Protoss.Corsair) },
        new If(
          new UnitsAtMost(0, Protoss.DarkTemplar),
          new ControlEnemyAirspace { flyers.get.unitMatcher.set(Protoss.Corsair) }))),
  
    new ClearBurrowedBlockers,
    new FindExpansions { scouts.get.unitMatcher.set(Protoss.DarkTemplar) },
    new DefendZones,
    new EscortSettlers,
    new If(
      new And(
        new EnemyUnitsAtMost(0, Zerg.Scourge),
        new UpgradeComplete(Protoss.ShuttleSpeed)),
      new DropAttack),
    new ConsiderAttacking,
  
    //TODO: Kill
  
    new ClearBurrowedBlockers,
    new FollowBuildOrder,
    new DefendAgainstProxy,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}

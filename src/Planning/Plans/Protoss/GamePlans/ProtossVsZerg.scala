package Planning.Plans.Protoss.GamePlans

import Information.StrategyDetection.ZergStrategies._
import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Reactive.{EnemyBasesAtLeast, EnemyMassMutalisks, EnemyMutalisks}
import Planning.Plans.Information.Scenarios.EnemyStrategy
import Planning.Plans.Information.{Employ, Employing, StartPositionsAtLeast}
import Planning.Plans.Macro.Automatic.{MatchingRatio, _}
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{Defend2GateAgainst4Pool, DefendFFEAgainst4Pool, ForgeFastExpand, TwoGatewaysAtNexus}
import Planning.Plans.Scouting.{FindExpansions, Scout, ScoutAt}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZ._

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs. Zerg")
  
  ////////////////
  // Early game //
  ////////////////
  
  private class ImplementEarly2Gate extends FirstEightMinutes(
    new Parallel(
      new TwoGatewaysAtNexus,
      new Trigger(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
        initialBefore = new Build(ProtossBuilds.OpeningTwoGate99_WithZealots: _*))))
  
  private class FFEFollowUp extends Build(
    RequestAtLeast(1, Protoss.Forge),
    RequestAtLeast(2, Protoss.PhotonCannon),
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(3, Protoss.PhotonCannon))
  
  private class FFE extends FirstEightMinutes(
    new Parallel(
      new ForgeFastExpand,
      new If(
        new EnemyStrategy(Fingerprint4Pool),
        new Build(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Employing(PvZEarlyFFEGatewayFirst),
        new Build(ProtossBuilds.FFE_GatewayFirst: _*),
      new If(
        new EnemyStrategy(Fingerprint12Hatch),
        new Build(ProtossBuilds.FFE_NexusFirst: _*),
      new If(
        new Or(
          new EnemyStrategy(Fingerprint9Pool),
          new EnemyStrategy(FingerprintOverpool),
          new EnemyStrategy(Fingerprint10Hatch9Pool)),
        new Build(ProtossBuilds.FFE_ForgeFirst: _*),
      new If(
        new Employing(PvZEarlyFFEConservative),
        new Build(ProtossBuilds.FFE_Vs4Pool: _*),
      new If(
        new Employing(PvZEarlyFFENexusFirst),
        new Build(ProtossBuilds.FFE_NexusFirst: _*),
        new Build(ProtossBuilds.FFE_ForgeFirst: _*))))))),
      new RequireMiningBases(2),
      new FFEFollowUp
    ))
  
  private class ImplementEarlyFFEEconomic extends FFE
  private class ImplementEarlyFFEConservative extends FFE
  private class ImplementEarlyFFEGatewayFirst extends FFE
  private class ImplementEarlyFFENexusFirst extends FFE
  
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
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway))
  
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
    new UnitsAtLeast(18, UnitMatchWarriors),
    new RequireMiningBases(3))
  
  private class BuildDetectionForLurkers extends If(
    new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Lurker)),
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Observer)))
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
  
    new If(
      new HaveTech(Protoss.PsionicStorm),
      new MeldArchons(40),
      new MeldArchons),
    
    /////////////////////////////
    // Early game build orders //
    /////////////////////////////
    
    new RequireMiningBases(1),
    new Employ(PvZEarly2Gate,           new ImplementEarly2Gate),
    new Employ(PvZEarlyFFEEconomic,     new ImplementEarlyFFEEconomic),
    new Employ(PvZEarlyFFEConservative, new ImplementEarlyFFEConservative),
    new Employ(PvZEarlyFFENexusFirst,   new ImplementEarlyFFENexusFirst),
    new Employ(PvZEarlyFFEGatewayFirst, new ImplementEarlyFFEGatewayFirst),
  
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
              MatchingRatio(UnitMatchType(Zerg.Hydralisk), 0.5))),
          new If(
            new Not(new EnemyBasesAtLeast(2)),
            new TrainContinuously(Protoss.PhotonCannon, 6))))),
  
    new FirstEightMinutes(
      new If(
        new And(
          new EnemyStrategy(Fingerprint4Pool),
          new Check(() => With.frame > 24 * 125),
          new UnitsAtMost(1, UnitMatchType(Protoss.PhotonCannon), complete = true)),
        new DefendFFEAgainst4Pool)),
    
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
      new And(
        new UnitsAtLeast(4, UnitMatchWarriors, complete = false),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Forge), complete = true),
        new UnitsAtLeast(1, UnitMatchType(Protoss.Assimilator), complete = true)),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.ZealotSpeed))),
  
    new If(
      new And(
        new Employing(PvZMidgameCorsairDarkTemplar),
        new UnitsAtLeast(2, UnitMatchType(Protoss.DarkTemplar), complete = true)),
      new TrainMatchingRatio(Protoss.Corsair, 5, Int.MaxValue, Seq(MatchingRatio(UnitMatchType(Zerg.Mutalisk), 1.5))),
      new TrainMatchingRatio(Protoss.Corsair, 1, Int.MaxValue, Seq(MatchingRatio(UnitMatchType(Zerg.Mutalisk), 1.5)))),
  
    new If(
      new EnemyMassMutalisks,
      new Build(RequestAtLeast(2, Protoss.Stargate))),
    
    new TrainMatchingRatio(Protoss.Observer, 0, 3, Seq(MatchingRatio(UnitMatchType(Zerg.Lurker), 0.5))),
  
    new If(
      new And(
        new EnemyMutalisks,
        new UnitsAtMost(5, UnitMatchType(Protoss.Corsair))),
      new TrainContinuously(Protoss.Dragoon),
      new Parallel(
        new If(
          new Not(new Employing(PvZMidgameCorsairSpeedlot)),
          new TrainContinuously(Protoss.DarkTemplar, 3)),
        new TrainContinuously(Protoss.Reaver, 5),
        new If(
          new And(
            new UnitsAtLeast(1, UnitMatchType(Protoss.TemplarArchives), complete = true),
            new UnitsAtMost(3, UnitMatchType(Protoss.Archon), complete = false),
            new Check(() => With.self.gas * 2 > With.self.minerals)),
          new TrainContinuously(Protoss.HighTemplar, 8)),
          new If(
            new And(
              new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore), complete = true),
              new Or(
                new And(
                  new Employing(PvZMidgame5GateDragoons),
                  new UnitsAtMost(15, UnitMatchType(Protoss.Dragoon)),
                  new Check(() => With.self.minerals < With.self.gas * 5)),
                new And(
                  new Employing(PvZMidgame5GateDragoons),
                  new UnitsAtMost(15, UnitMatchType(Protoss.Dragoon)),
                  new Check(() => With.self.minerals < With.self.gas * 5)),
                new UnitsAtLeast(30, UnitMatchType(Protoss.Zealot))
              )
            ),
            new TrainContinuously(Protoss.Dragoon),
            new TrainContinuously(Protoss.Zealot)))),
  
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore)),
      new TrainMatchingRatio(Protoss.Gateway, 1, 5, Seq(
          MatchingRatio(UnitMatchType(Zerg.Hydralisk), 0.3),
          MatchingRatio(UnitMatchType(Zerg.Zergling), 0.25)))),
    new Employ(PvZMidgame5GateDragoons,         new ImplementMidgame5GateDragoons),
    new Employ(PvZMidgameCorsairDarkTemplar,    new ImplementMidgameCorsairDarkTemplar),
    new Employ(PvZMidgameCorsairReaver,         new ImplementMidgameCorsairReaver),
    new Employ(PvZMidgameCorsairSpeedlot,       new ImplementMidgameCorsairSpeedlot),
    new BuildAssimilators,
  
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
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestUpgrade(Protoss.HighTemplarEnergy),
      RequestTech(Protoss.PsionicStorm),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Observer),
      RequestAtLeast(12, Protoss.Gateway)),
  
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
  
    /////////////
    // Tactics //
    /////////////
    
    new If(
      new Or(
        new Employing(PvZEarlyFFEEconomic),
        new Employing(PvZEarlyFFEConservative),
        new Employing(PvZEarlyFFEGatewayFirst),
        new Employing(PvZEarlyFFENexusFirst)),
      new If(
        new Check(() => With.units.ours.exists(u => u.is(Protoss.Pylon) && With.framesSince(u.frameDiscovered) > 24)),
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
    
    new ControlMap(attack = false),
    new If(
      new UnitsAtLeast(4, UnitMatchWarriors, complete = true),
      new ConsiderAttacking)
  ))
}

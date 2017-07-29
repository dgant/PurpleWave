package Planning.Plans.Protoss.GamePlans

import Information.StrategyDetection.Fingerprint4Pool
import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Scenarios.{EnemyStrategy}
import Planning.Plans.Information.{Employ, Employing, StartPositionsAtLeast}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstFiveMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Reaction.{EnemyBasesAtLeast, EnemyMassMutalisks, EnemyMutalisks}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{Defend2GateAgainst4Pool, DefendFFEAgainst4Pool, ForgeFastExpand, TwoGatewaysAtNexus}
import Planning.Plans.Scouting.{FindExpansions, Scout, ScoutAt}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Options.Protoss.PvZ._

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs. Zerg")
  
  ////////////////
  // Early game //
  ////////////////
  
  private class ImplementEarly2Gate extends FirstFiveMinutes(
    new Parallel(
      new TwoGatewaysAtNexus,
      new Trigger(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
        initialBefore = new Build(ProtossBuilds.OpeningTwoGate99_WithZealots: _*))))
  
  private class ImplementEarlyFFELight extends FirstFiveMinutes(
    new Parallel(
      new ForgeFastExpand(cannonsInFront = false),
      new If(
        new Or(
          new Employing(PvZEarlyFFEHeavy),
          new EnemyStrategy(Fingerprint4Pool)),
        new Build(ProtossBuilds.FFE_Vs4Pool: _*),
        new If(
          new EnemyBasesAtLeast(2),
          new Build(ProtossBuilds.FFE_NexusFirst: _*),
          new Build(ProtossBuilds.FFE_ForgeFirst: _*))),
      new RequireMiningBases(2),
      new Build(
        RequestAtLeast(2, Protoss.PhotonCannon),
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(3, Protoss.PhotonCannon))))
  
  private class ImplementEarlyFFEHeavy extends ImplementEarlyFFELight
  
  /////////////
  // Midgame //
  /////////////
  
  private class ImplementMidgame5GateDragoons extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Zealot),
      RequestUpgrade(Protoss.DragoonRange)),
    new If(
      new MiningBasesAtLeast(2),
      new Parallel(
        new BuildAssimilators,
        new Build(RequestAtLeast(5, Protoss.Gateway))),
      new Build(RequestAtLeast(3, Protoss.Gateway))))
  
  private class ImplementMidgameCorsairCarrier extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(4, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(2, Protoss.Stargate),
      RequestUpgrade(Protoss.AirDamage),
      RequestUpgrade(Protoss.CarrierCapacity)))
  
  private class ImplementMidgameCorsairSpeedlot extends Parallel(
    new If(
      new MiningBasesAtLeast(2),
      new Parallel(
        new BuildAssimilators,
        new Build(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(2, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestUpgrade(Protoss.GroundDamage),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(5, Protoss.Gateway))),
      new Build(RequestAtLeast(3, Protoss.Gateway))))
  
  private class ImplementMidgameCorsairReaver extends Parallel(
    new If(
      new MiningBasesAtLeast(2),
      new Parallel(
        new BuildAssimilators,
        new Build(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestUpgrade(Protoss.GroundDamage),
          RequestAtLeast(1, Protoss.RoboticsSupportBay),
          RequestAtLeast(5, Protoss.Gateway))),
      new Build(RequestAtLeast(3, Protoss.Gateway))))
  
  private class ImplementMidgameCorsairDarkTemplar extends Parallel(
    new If(
      new MiningBasesAtLeast(2),
      new Parallel(
        new BuildAssimilators,
        new Build(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Assimilator),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(5, Protoss.Gateway))),
      new Build(RequestAtLeast(3, Protoss.Gateway))))
  
  ///////////
  // Macro //
  ///////////
  
  private class TakeSafeNatural extends If(
    new UnitsAtLeast(6, UnitMatchWarriors),
    new RequireMiningBases(2))
  
  private class TakeSafeThirdBase extends If(
    new UnitsAtLeast(14, UnitMatchWarriors),
    new RequireMiningBases(3))
  
  private class BuildDetectionForLurkers extends If(
    new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Lurker)),
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)))
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
  
    new If(
      new HaveTech(Protoss.PsionicStorm),
      new MeldArchons(40),
      new MeldArchons),
    
    // Early game
    new RequireMiningBases(1),
    new Employ(PvZEarlyZealotAllIn,  new ImplementEarly2Gate),
    new Employ(PvZEarly2Gate,        new ImplementEarly2Gate),
    new Employ(PvZEarlyFFELight,     new ImplementEarlyFFELight),
    new Employ(PvZEarlyFFEHeavy,     new ImplementEarlyFFEHeavy),
    
    // Build cannons vs. Zergling rushes
    new FirstFiveMinutes(
      new Parallel(
        new If(
          new Or(
            new Employing(PvZEarlyFFELight),
            new Employing(PvZEarlyFFEHeavy)),
          new TrainMatchingRatio(Protoss.PhotonCannon, UnitMatchType(Zerg.Zergling), 0.5, 6)),
        new If(
          new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore), complete = false),
          new Build(RequestAtLeast(4, Protoss.PhotonCannon))
        ))),
  
    // 4/5-pool defense
    new If(
      new And(
        new EnemyStrategy(Fingerprint4Pool),
        new Check(() => With.frame > 24 * 125),
        new UnitsAtMost(1, UnitMatchType(Protoss.PhotonCannon), complete = true)),
      new DefendFFEAgainst4Pool),
    new FirstFiveMinutes(new Defend2GateAgainst4Pool),
    
    // Early game macro
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new If(
      new Not(new Employing(PvZEarlyZealotAllIn)),
      new Parallel(
        new TakeSafeNatural,
        new TakeSafeThirdBase)),
  
    // #YOLO
    new Employ(PvZEarlyZealotAllIn, new Parallel(
      new TrainContinuously(Protoss.Zealot),
      new TrainContinuously(Protoss.Gateway, 5),
      new Trigger(
        new Or(
          new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Mutalisk)),
          new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Lurker)),
          new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Spire))),
        initialAfter = new AllIn)
    )),
  
    // Mid game builds
    new Employ(PvZMidgame5GateDragoons, new ImplementMidgame5GateDragoons),
    
    // Mid-game macro
    new FirstFiveMinutes(new Employ(PvZEarlyFFEHeavy, new Build(RequestAtLeast(1, Protoss.Gateway), RequestAtLeast(5, Protoss.PhotonCannon)))),
    new BuildDetectionForLurkers,
    new BuildCannonsAtExpansions(5),
  
    new If(
      new UnitsAtLeast(3, UnitMatchType(Protoss.Carrier), complete = false),
      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    
    new If(
      new UnitsAtLeast(6, UnitMatchType(Protoss.Carrier), complete = false),
      new UpgradeContinuously(Protoss.AirDamage)),
  
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
      new EnemyMutalisks,
      new TrainMatchingRatio(Protoss.Corsair, UnitMatchType(Zerg.Mutalisk), 1.5),
      new If(
        new And(
          new Employing(PvZMidgameCorsairDarkTemplar),
          new UnitsAtLeast(2, UnitMatchType(Protoss.DarkTemplar), complete = true)),
        new TrainContinuously(Protoss.Corsair, 5),
        new TrainContinuously(Protoss.Corsair, 2))),
  
    new If(
      new EnemyMassMutalisks,
      new Build(RequestAtLeast(2, Protoss.Stargate))),
    
    new TrainContinuously(Protoss.Carrier),
    new TrainMatchingRatio(Protoss.Observer, UnitMatchType(Zerg.Lurker), 0.2, 3),
  
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
    
    new Employ(PvZMidgameCorsairCarrier,        new ImplementMidgameCorsairCarrier),
    new Employ(PvZMidgameCorsairDarkTemplar,    new ImplementMidgameCorsairDarkTemplar),
    new Employ(PvZMidgameCorsairReaver,         new ImplementMidgameCorsairReaver),
    new Employ(PvZMidgameCorsairSpeedlot,       new ImplementMidgameCorsairSpeedlot),
    new BuildAssimilators,
    
    // Late game macro
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestUpgrade(Protoss.HighTemplarEnergy),
      RequestTech(Protoss.PsionicStorm),
      RequestUpgrade(Protoss.HighTemplarEnergy),
      RequestAtLeast(10, Protoss.Gateway)),
    
    new UpgradeContinuously(Protoss.GroundArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    
    // Zealot all-in: Trigger attacking immediately! And bring Probes because they help kill Zerglings faster
    new Employ(PvZEarlyZealotAllIn,
      new Trigger(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
        new Parallel(
          new ConsiderAttacking {
            whenFalse.set(
              new If(
                new UnitsAtLeast(6, UnitMatchType(Protoss.Zealot), complete = true),
                new DefendChokes(1),
                new DefendHearts
              )) },
          new If (
            new And(
              new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
              new Check(() => With.frame < 24 * 60 * 4)),
            new ConsiderAttacking {
              attack.attackers.get.unitCounter.set(new UnitCountBetween(1, 2))
              attack.attackers.get.unitMatcher.set(UnitMatchWorkers)
            })),
        new DefendHearts)),
  
    new If(
      new Or(
        new Employing(PvZEarlyFFELight),
        new Employing(PvZEarlyFFEHeavy)),
      new If(
        new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
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

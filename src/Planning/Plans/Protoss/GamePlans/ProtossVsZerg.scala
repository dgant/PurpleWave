package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.{Employ, Employing}
import Planning.Plans.Information.Scenarios.WeAreBeing4Pooled
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstFiveMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Reaction.{EnemyBasesAtLeast, EnemyMutalisks}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{DefendChokeWithWorkers, ForgeFastExpand}
import Planning.Plans.Scouting.{FindExpansions, RequireScouting}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Options.Protoss.PvZ._

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs. Zerg")
  
  ////////////////
  // Early game //
  ////////////////
  
  private class ImplementEarly2Gate extends FirstFiveMinutes(
    new Build(ProtossBuilds.OpeningTwoGate99_WithZealots: _*))
  
  private class ImplementEarlyFFELight extends FirstFiveMinutes(
    new Parallel(
      new If(
        new WeAreBeing4Pooled,
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
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(3, Protoss.Stargate),
      RequestUpgrade(Protoss.AirDamage),
      RequestUpgrade(Protoss.CarrierCapacity),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed)))
  
  private class ImplementMidgameCorsairSpeedlot extends Parallel(
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
    new UnitsAtLeast(20, UnitMatchWarriors),
    new RequireMiningBases(3))
  
  private class BuildDetectionForLurkers extends If(
    new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Lurker)),
    new Build(
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)))

  private class BuildAntiAirForMutalisks extends If(
    new EnemyMutalisks,
    new Build
  )
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
  
    new MeldArchons,
    
    // Early game
    new RequireMiningBases(1),
    new ForgeFastExpand(cannonsInFront = true), // Do we always want this placement?
    new Employ(PvZEarlyZealotAllIn,  new ImplementEarly2Gate),
    new Employ(PvZEarly2Gate,        new ImplementEarly2Gate),
    new Employ(PvZEarlyFFELight,     new ImplementEarlyFFELight),
    new Employ(PvZEarlyFFEHeavy,     new ImplementEarlyFFEHeavy),
    
    // Build cannons vs. Zergling rushes
    new If(
      new Or(
        new Employing(PvZEarlyFFELight),
        new Employing(PvZEarlyFFEHeavy)),
      new TrainMatchingRatio(Protoss.PhotonCannon, UnitMatchType(Zerg.Zergling), 0.5, 6)),
    
    new TakeSafeNatural,
    new TakeSafeThirdBase,
    
    // Early game macro
    new RequireSufficientPylons,
    new TrainProbesContinuously,
  
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
    new BuildAssimilators,
    new FirstFiveMinutes(new Employ(PvZEarlyFFEHeavy, new Build(RequestAtLeast(1, Protoss.Gateway), RequestAtLeast(6, Protoss.PhotonCannon)))),
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
    
    new TrainContinuously(Protoss.Carrier),
    new TrainMatchingRatio(Protoss.Observer, UnitMatchType(Zerg.Lurker), 0.2, 3),
  
    new If(
      new EnemyMutalisks,
      new TrainContinuously(Protoss.Dragoon),
      new Parallel(
        new If(
          new Not(new Employing(PvZMidgameCorsairSpeedlot)),
          new TrainContinuously(Protoss.DarkTemplar, 3)),
        new TrainContinuously(Protoss.Reaver, 5),
        new If(
          new Check(() =>
            With.units.ours.exists(u => u.is(Protoss.TemplarArchives) && u.aliveAndComplete) &&
              With.self.gas > With.self.minerals),
          new TrainContinuously(Protoss.HighTemplar, 4)),
        new If(
          new And(
            new Check(() => With.self.minerals > With.self.gas * 5),
            new Or(
              new Employing(PvZMidgame5GateDragoons),
              new Employing(PvZMidgame5GateDragoons),
              new UnitsAtLeast(12, UnitMatchType(Protoss.Zealot)))),
          new TrainContinuously(Protoss.Dragoon),
          new TrainContinuously(Protoss.Zealot)))),
    
    new Employ(PvZMidgameCorsairCarrier,        new ImplementMidgameCorsairCarrier),
    new Employ(PvZMidgameCorsairDarkTemplar,    new ImplementMidgameCorsairDarkTemplar),
    new Employ(PvZMidgameCorsairReaver,         new ImplementMidgameCorsairReaver),
    new Employ(PvZMidgameCorsairSpeedlot,       new ImplementMidgameCorsairSpeedlot),
    
    
    // Late game macro
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.TemplarArchives)),
    
    new UpgradeContinuously(Protoss.GroundArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    
    // Tactics
    new If(
      new And(
        new WeAreBeing4Pooled,
        new UnitsAtMost(1, UnitMatchType(Protoss.PhotonCannon), complete = true)),
      new If(
        new Check(() => With.frame > 24 * (2 * 60)), // When a 4-pool arrives on a tiny rush distance
        new DefendChokeWithWorkers),
      new If(
        new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
        new RequireScouting)), // Don't scout while being 4-pooled
    
    // Zealot all-in: Trigger attacking immediately! And bring Probes because they help kill Zerglings faster
    new Employ(PvZEarlyZealotAllIn,
      new Trigger(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
        new Parallel(
          new ConsiderAttacking {
            whenFalse.set(new DefendHearts)
          },
          new If (
            new Check(() => With.frame < 24 * 60 * 4),
            new ConsiderAttacking {
              attack.attackers.get.unitCounter.set(new UnitCountBetween(1, 2))
              attack.attackers.get.unitMatcher.set(UnitMatchWorkers)
            })),
        new DefendHearts)),
  
    new If(
      new And(
        new Or(
          new UnitsAtMost(0, UnitMatchType(Protoss.DarkTemplar), complete = true),
          new Not(new Employing(PvZMidgameCorsairDarkTemplar))),
        new EnemyUnitsAtMost(0, UnitMatchType(Zerg.Spire), complete = true),
        new EnemyUnitsAtMost(0, UnitMatchType(Zerg.Mutalisk)),
        new EnemyUnitsAtMost(0, UnitMatchType(Zerg.Scourge))),
      new Parallel(
        new FindExpansions       { scouts.get.unitMatcher.set(UnitMatchType(Protoss.Corsair)) },
        new ControlEnemyAirspace { flyers.get.unitMatcher.set(UnitMatchType(Protoss.Corsair)) })),
    
    new ControlMap,
    new If(
      new UnitsAtLeast(4, UnitMatchWarriors, complete = true),
      new ConsiderAttacking)
  ))
}

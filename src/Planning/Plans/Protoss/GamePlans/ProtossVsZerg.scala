package Planning.Plans.Protoss.GamePlans

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Employ
import Planning.Plans.Information.Scenarios.WeAreBeing4Pooled
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstFiveMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Reaction.{EnemyBasesAtLeast, EnemyMutalisks}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{DefendChokeWithWorkers, ForgeFastExpand}
import Planning.Plans.Scouting.RequireScouting
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
      new ForgeFastExpand,
      new If(
        new EnemyBasesAtLeast(2),
        new Build(ProtossBuilds.FFE_NexusFirst: _*),
        new Build(ProtossBuilds.FFE_ForgeFirst: _*)),
      new RequireMiningBases(2)))
  
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
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(1, Protoss.Dragoon)),
    new If(
      new MiningBasesAtLeast(2),
      new Parallel(
        new Build(RequestAtLeast(2, Protoss.Assimilator)),
        new Build(RequestAtLeast(5, Protoss.Gateway))),
      new Build(RequestAtLeast(3, Protoss.Gateway))))
  
  private class ImplementMidgameCorsairCarrier extends Parallel(
    new Build(
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(5, Protoss.PhotonCannon),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(3, Protoss.Stargate),
      RequestUpgrade(Protoss.AirDamage),
      RequestUpgrade(Protoss.CarrierCapacity),
      RequestAtLeast(4, Protoss.Stargate)))
  
  private class ImplementMidgameCorsairSpeedlot extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.GroundDamage)),
    new If(
      new MiningBasesAtLeast(2),
      new Build(
        RequestAtLeast(2, Protoss.Assimilator),
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Stargate),
        RequestAtLeast(1, Protoss.CitadelOfAdun)),
      new Build(RequestAtLeast(3, Protoss.Gateway))))
  
  private class ImplementMidgameCorsairReaver extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(4, Protoss.PhotonCannon),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)))
  
  ///////////
  // Macro //
  ///////////
  
  private class TakeSafeNatural extends If(
    new UnitsAtLeast(12, UnitMatchWarriors),
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
    
    // Early game
    new RequireMiningBases(1),
    new Employ(EarlyZealotAllIn,  new ImplementEarly2Gate),
    new Employ(Early2Gate,        new ImplementEarly2Gate),
    new Employ(EarlyFFELight,     new ImplementEarlyFFELight),
    new Employ(EarlyFFEHeavy,     new ImplementEarlyFFEHeavy),
    new TakeSafeNatural,
    new TakeSafeThirdBase,
    
    
    // Early game macro
    new RequireSufficientPylons,
    new TrainProbesContinuously,
  
    // #YOLO
    new Employ(EarlyZealotAllIn, new Parallel(
      new TrainContinuously(Protoss.Zealot),
      new TrainContinuously(Protoss.Gateway, 5))),
  
    // Mid game builds
    new Employ(Midgame5GateDragoons, new ImplementMidgame5GateDragoons),
    
    // Mid-game macro
    new BuildAssimilators,
    new Employ(EarlyFFEHeavy, new Build(RequestAtLeast(8, Protoss.PhotonCannon))),
    new BuildDetectionForLurkers,
  
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
      new UnitsAtLeast(8, UnitMatchType(Protoss.Zealot), complete = false),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new Build(RequestUpgrade(Protoss.ZealotSpeed)))),
    
    new If(
      new EnemyMutalisks,
      new TrainMatchingRatio(Protoss.Corsair, UnitMatchType(Zerg.Mutalisk), 1.5),
      new TrainContinuously(Protoss.Corsair, 1)),
    
    new TrainContinuously(Protoss.Carrier),
    new TrainMatchingRatio(Protoss.Observer, UnitMatchType(Zerg.Lurker), 0.2, 3),
  
    new If(
      new EnemyMutalisks,
      new TrainContinuously(Protoss.Dragoon),
      new Parallel(
        new TrainContinuously(Protoss.Reaver, 5),
        new If(
          new UnitsAtLeast(12, UnitMatchType(Protoss.Zealot)),
          new TrainContinuously(Protoss.Dragoon),
          new TrainContinuously(Protoss.Zealot)))),
  
    new Employ(MidgameCorsairReaver,    new ImplementMidgameCorsairReaver),
    new Employ(MidgameCorsairSpeedlot,  new ImplementMidgameCorsairSpeedlot),
    new Employ(MidgameCorsairCarrier,   new ImplementMidgameCorsairCarrier),
    
    // Late game macro
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.RoboticsFacility)),
    
    new Build(
      RequestAtLeast(2, Protoss.Forge),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives)),
    
    new UpgradeContinuously(Protoss.GroundArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    
    
    // Tactics
    new If(
      new And(
        new WeAreBeing4Pooled,
        new UnitsAtMost(2, UnitMatchType(Protoss.PhotonCannon), complete = true)),
      new DefendChokeWithWorkers,
      new If(
        new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
        new RequireScouting)), // Don't scout while being 4-pooled
    
    
    // Zealot all-in: Trigger attacking immediately! And bring Probes because they help kill Zerglings faster
    new Employ(EarlyZealotAllIn,
      new Trigger(
        new UnitsAtLeast(2, UnitMatchType(Protoss.Zealot), complete = true),
        new Parallel(
          new ConsiderAttacking {
            whenFalse.set(new DefendHearts)
          },
          new ConsiderAttacking {
            attack.attackers.get.unitCounter.set(new UnitCountBetween(1, 2))
            attack.attackers.get.unitMatcher.set(UnitMatchWorkers)
          }),
        new DefendHearts)),
    
    new ControlMap,
    new If(
      new UnitsAtLeast(4, UnitMatchWarriors, complete = true),
      new ConsiderAttacking)
  ))
}

package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational._
import Planning.Plans.Macro.Automatic.{MatchingRatio, _}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, BuildCannonsAtExpansions}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.{GasAtLeast, GasAtMost, MineralsAtLeast, MineralsAtMost}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyMutalisks
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Predicates.{Employ, Employing}
import Planning.Plans.Scouting.FindExpansions
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZ._

class ProtossVsZergOld extends GameplanModeTemplate {
  
  private class WeFFEd extends Or(
    new Employing(PvZEarlyFFEEconomic),
    new Employing(PvZEarlyFFEConservative),
    new Employing(PvZEarlyFFEGatewayFirst),
    new Employing(PvZEarlyFFENexusFirst))
  
  private class ImplementMidgame5GateDragoons extends Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(3, Protoss.Gateway),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(5, Protoss.Gateway))
  
  private class ImplementMidgameCorsairSpeedlot extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.GroundDamage),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgameCorsairReaver extends Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(2, Protoss.Assimilator),
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
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgame2Stargate extends Build(
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.Assimilator),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(2, Protoss.Assimilator),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(2, Protoss.Gateway),
    RequestAtLeast(2, Protoss.Nexus),
    RequestAtLeast(1, Protoss.CitadelOfAdun),
    RequestAtLeast(4, Protoss.Gateway),
    RequestAtLeast(2, Protoss.Stargate))
  
  override def aggression: Double = 0.75
  
  override def defaultPlacementPlan: Plan = new If(
    new WeFFEd,
    new PlacementForgeFastExpand)
  
  override def priorityDefensePlan: Plan = new Parallel(
    new FirstEightMinutes(
      new If(
        new And(
          new WeFFEd,
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint4Pool),
          new FrameAtLeast(GameTime(2, 5)()),
          new UnitsAtLeast(1, Protoss.PhotonCannon, complete = false),
          new UnitsAtMost(2, Protoss.PhotonCannon, complete = true)),
        new DefendFFEWithProbesAgainst4Pool)),
  
    new FirstEightMinutes(
      new If(
        new And(
          new EnemyUnitsAtLeast(4, Zerg.Zergling),
          new WeFFEd,
          new Or(
            new EnemyStrategy(With.intelligence.fingerprints.fingerprint9Pool),
            new EnemyStrategy(With.intelligence.fingerprints.fingerprintOverpool)),
          new EnemyStrategy(With.intelligence.fingerprints.fingerprint10Hatch9Pool),
          new FrameAtLeast(GameTime(2, 5)()),
          new UnitsAtMost(2, Protoss.PhotonCannon, complete = true)),
        new DefendFFEWithProbesAgainst9Pool)),
  
    new FirstEightMinutes(new DefendZealotsAgainst4Pool))
  
  override def buildPlans: Seq[Plan] = Vector(
    new FirstEightMinutes(
      new If(
        new UnitsAtMost(2, Protoss.Gateway, complete = true),
        new Parallel(
          new TrainMatchingRatio(Protoss.PhotonCannon, 2, 6,
            Seq(
              MatchingRatio(Zerg.Zergling, 0.3),
              MatchingRatio(Zerg.Hydralisk, 0.75)))))),
    
    new PvZIdeas.BuildDetectionForLurkers,
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.TakeSafeThirdBase,
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
        new UpgradeContinuously(Protoss.ZealotSpeed),
        new If(
          new UnitsAtLeast(2, Protoss.Forge),
          new UpgradeContinuously(Protoss.GroundArmor)))),
  
    new If(
      new And(
        new UnitsAtLeast(6, Protoss.Corsair),
        new Or(
          new UnitsAtLeast(3, Protoss.Reaver),
          new Employing(PvZMidgame2Stargate))),
      new TrainContinuously(Protoss.Carrier)),
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
  
    new OnGasPumps(2,
      new If(
        new EnemyUnitsAtLeast(18, Zerg.Mutalisk),
        new Build(RequestAtLeast(3, Protoss.Stargate)),
        new If(
          new EnemyUnitsAtLeast(10, Zerg.Mutalisk),
          new Build(RequestAtLeast(2, Protoss.Stargate))))),
  
    new TrainMatchingRatio(Protoss.Observer, 0, 3, Seq(MatchingRatio(Zerg.Lurker, 0.5))),
    
    // Gateway production
    new If(
      new And(
        new MineralsAtLeast(600),
        new GasAtMost(49)),
      new TrainContinuously(Protoss.Zealot)),
    new If(
      // Emergency Dragoons
      new And(
        new EnemyMutalisks,
        new UnitsAtMost(5, Protoss.Corsair),
        new UnitsAtMost(8, Protoss.Dragoon)),
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
          new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true),
          new TrainContinuously(Protoss.HighTemplar, 12, 3)),
        new If(
          new And(
            new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
            new Or(
              new Employing(PvZMidgame5GateDragoons),
              new Employing(PvZMidgameCorsairReaver)),
            new UnitsAtMost(10, Protoss.Dragoon),
            new Or(new GasAtLeast(100), new MineralsAtMost(400))),
          new TrainContinuously(Protoss.Dragoon),
          new TrainContinuously(Protoss.Zealot)))),
  
    new Employ(PvZMidgame5GateDragoons,       new ImplementMidgame5GateDragoons),
    new Employ(PvZMidgameCorsairDarkTemplar,  new ImplementMidgameCorsairDarkTemplar),
    new Employ(PvZMidgameCorsairReaver,       new ImplementMidgameCorsairReaver),
    new Employ(PvZMidgameCorsairSpeedlot,     new ImplementMidgameCorsairSpeedlot),
    new Employ(PvZMidgame2Stargate,           new ImplementMidgame2Stargate),
    new If(
      new Or(
        new UnitsAtLeast(32, UnitMatchWorkers),
        new MineralsAtLeast(1000)),
      new BuildGasPumps),
    
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
      new And(
        new EnemyUnitsAtMost(0, Zerg.Spire, complete = true),
        new EnemyUnitsAtMost(0, Zerg.Mutalisk),
        new EnemyUnitsAtMost(0, Zerg.Scourge)),
      new Parallel(
        new FindExpansions { scouts.get.unitMatcher.set(Protoss.Corsair) },
        new If(
          new UnitsAtMost(0, Protoss.DarkTemplar),
          new ControlEnemyAirspace { flyers.get.unitMatcher.set(Protoss.Corsair) }))),
    new FindExpansions { scouts.get.unitMatcher.set(Protoss.DarkTemplar) }
  )
}

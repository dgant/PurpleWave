package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Information.{Employ, Employing}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, BuildCannonsAtBases, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, _}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.{ProtossBuilds, ProtossVsTerranIdeas}
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT._

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs. Terran")
  
  ///////////////////////////
  // Early-game strategies //
  ///////////////////////////
  
  private class ImplementEarly14Nexus       extends FirstEightMinutes(new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*))
  private class ImplementEarly1GateRange    extends FirstEightMinutes(new Build(ProtossBuilds.Opening_1GateCore: _*))
  private class ImplementEarly1015GateGoon  extends FirstEightMinutes(new Build(ProtossBuilds.OpeningTwoGate1015Dragoons: _*))
  private class ImplementEarlyDTExpand      extends FirstEightMinutes(new Build(ProtossBuilds.OpeningDTExpand: _*))
  private class ImplementEarly4GateAllIn    extends FirstEightMinutes(new Build(ProtossBuilds.Opening_1GateZZCore: _*))
  
  private class FulfillEarlyTech extends Build(
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange))
  
  /////////////////////////
  // Mid-game strategies //
  /////////////////////////
  
  private class TakeThirdBaseSafely extends If(
    new Or(
      new UnitsAtLeast(5, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(3))
  
  private class FulfillMidgameTech extends Build(
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.RoboticsFacility),
    RequestAtLeast(1,   Protoss.Observatory),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(1,   Protoss.Forge),
    RequestUpgrade(Protoss.ZealotSpeed))
  
  //////////////////////////
  // Late-game strategies //
  //////////////////////////
  
  private class ImplementLateCarriers extends OnMiningBases(3,
    new Parallel(
      new Build(
        RequestAtLeast(8, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Stargate),
        RequestAtLeast(1, Protoss.FleetBeacon),
        RequestAtLeast(3, Protoss.Stargate),
        RequestUpgrade(Protoss.AirDamage, 1),
        RequestUpgrade(Protoss.CarrierCapacity)),
      new If(
        new EnemyBio,
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.AirDamage))))
  
  private class ImplementLateArbiters extends OnGasBases(3,
    new Build(
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.ArbiterTribunal)))
  
  private class OnThreeBases_Speedlots extends OnMiningBases(3,
    new Build(
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed)))
  
  private class OnThreeBases_WeaponsUpgrades extends OnMiningBases(3,
    new Build(
      RequestAtLeast(1, Protoss.Forge),
      RequestUpgrade(Protoss.GroundDamage)))
  
  private class CompleteUpgrades extends Build(
    RequestAtLeast(1, Protoss.Forge),
    RequestUpgrade(Protoss.GroundDamage, 1),
    RequestAtLeast(1, Protoss.CitadelOfAdun),
    RequestAtLeast(1, Protoss.TemplarArchives),
    RequestUpgrade(Protoss.GroundDamage, 2),
    RequestUpgrade(Protoss.GroundDamage, 3),
    RequestUpgrade(Protoss.GroundArmor, 1),
    RequestUpgrade(Protoss.GroundArmor, 2),
    RequestUpgrade(Protoss.GroundArmor, 3))
  
  ///////////////
  // Responses //
  ///////////////
  
  private class IfNoDetection_DarkTemplar extends If(
    new And(
      new EnemyUnitsNone(UnitMatchType(Terran.ScienceVessel)),
      new EnemyUnitsNone(UnitMatchType(Terran.MissileTurret))),
    new TrainContinuously(Protoss.DarkTemplar, 3),
    new TrainContinuously(Protoss.DarkTemplar, 1))
  
  private class IfCloakedThreats_Observers extends If(
    new Or(
      new EnemyHasShown(Terran.SpiderMine),
      new EnemyHasTech(Terran.WraithCloak)),
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)))
  
  class TrainZealotsOrDragoons extends If(
    new Or(
      new And(
        new Employing(PvTEarly4GateAllIn),
        new Or(
          new UnitsAtMost(0, UnitMatchType(Protoss.CyberneticsCore), complete = true),
          new Check(() => With.self.gas < 50 && With.self.minerals > 200))),
      new And(
        new HaveUpgrade(Protoss.ZealotSpeed, withinFrames = Protoss.Zealot.buildFrames),
        new Or(
          new UnitsAtLeast(12, UnitMatchType(Protoss.Dragoon)),
          new Check(() => With.self.gas * 5 < With.self.minerals)))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon))
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
    new If(
      new And(
        new Employing(PvTEarly4GateAllIn),
        new Check(() => With.geography.enemyBases.size > 1)),
      new AllIn
    ),
    
    // Early game
    new RequireMiningBases(1),
    new Employ(PvTEarly14Nexus,      new ImplementEarly14Nexus),
    new Employ(PvTEarly1GateRange,   new ImplementEarly1GateRange),
    new Employ(PvTEarly1015GateGoon, new ImplementEarly1015GateGoon),
    new Employ(PvTEarlyDTExpand,     new ImplementEarlyDTExpand),
    new Employ(PvTEarly4GateAllIn,   new ImplementEarly4GateAllIn),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new Employ(PvTEarly4GateAllIn, new Parallel(
      new TrainZealotsOrDragoons,
      new Build(
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(4, Protoss.Gateway))
    )),
  
    new ProtossVsTerranIdeas.RespondToBioAllInWithReavers,
    
    // Mid game
    new If(
      new Not(new Employing(PvTEarly4GateAllIn)),
      new RequireMiningBases(2)),
  
    // Make sure we get an early Dragoon for Vulture defense
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore)),
      new Trigger(
        new UnitsAtLeast(1, UnitMatchType(Protoss.Dragoon)),
        initialBefore = new Build(RequestAtLeast(1, Protoss.Dragoon)))),
    
    new FulfillEarlyTech,
    
    // Mid game
     new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.HighTemplar), complete = false),
      new Build(RequestTech(Protoss.PsionicStorm))),
  
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Arbiter), complete = false),
      new Build(RequestTech(Protoss.Stasis))),
    
    new If(
      new Not(new Employing(PvTEarly4GateAllIn)),
      new MatchMiningBases(1)),
    new TakeThirdBaseSafely,
    
    new OnMiningBases(2,
      new Parallel(
        new ProtossVsTerranIdeas.RespondToBioWithReavers,
        new BuildAssimilators,
        new IfCloakedThreats_Observers,
        new If(
          new UnitsAtLeast(8, UnitMatchWarriors, complete = true),
          new FulfillMidgameTech),
        new BuildCannonsAtBases(1))),
    
    // Late game
    new If(
      new UnitsAtLeast(12, UnitMatchWarriors), // We have a habit of getting this tech too soon and dying
      new Parallel(
        new Employ(PvTLateArbiters, new ImplementLateArbiters),
        new Employ(PvTLateCarriers, new ImplementLateCarriers),
        new OnThreeBases_WeaponsUpgrades)),
    
    // Units
    new TrainContinuously(Protoss.Carrier),
    new IfNoDetection_DarkTemplar,
    new If(
      new EnemyBio,
      new TrainContinuously(Protoss.Reaver, 2)),
    new TrainContinuously(Protoss.Arbiter, 3),
    new If(
      new Or(
        new EnemyHasShown(Terran.SpiderMine),
        new EnemyHasTech(Terran.WraithCloak)),
      new TrainContinuously(Protoss.Observer, 3),
      new TrainContinuously(Protoss.Observer, 1)
    ),
    new TrainZealotsOrDragoons,
    
    // Luxuries
    new Build(RequestAtLeast(5, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Employ(PvTLateCarriers, new ImplementLateCarriers),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(15, Protoss.Gateway)),
    new CompleteUpgrades,
    
    // Tactics
    new ScoutAt(14),
    new ScoutExpansionsAt(100),
    new ClearBurrowedBlockers,
    new DefendZones,
    
    new Attack { attackers.get.unitMatcher.set(UnitMatchType(Protoss.DarkTemplar)) },
    new If(
      new And(
        new UnitsAtLeast(12, UnitMatchWarriors, complete = true),
        new Not(new EnemyBio),
        new Or(
          new UnitsAtLeast(1, UnitMatchType(Protoss.Observer), complete = true),
          new Not(new EnemyHasShown(Terran.SpiderMine)))),
      new Attack, // Contain 'em
      new ConsiderAttacking)
  ))
}
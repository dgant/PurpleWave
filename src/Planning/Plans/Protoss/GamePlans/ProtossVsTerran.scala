package Planning.Plans.Protoss.GamePlans

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{ConsiderAttacking, ControlMap}
import Planning.Plans.Compound.{And, If, Or, Parallel}
import Planning.Plans.Information.Employ
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstFiveMinutes}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Reaction.EnemyBio
import Planning.Plans.Protoss.{ProtossBuilds, ProtossVsTerranIdeas}
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Options.Protoss.PvT._

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs. Terran")
  
  ///////////////////////////
  // Early-game strategies //
  ///////////////////////////
  
  private class ImplementEarly14Nexus       extends FirstFiveMinutes(new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*))
  private class ImplementEarly1GateRange    extends FirstFiveMinutes(new Build(ProtossBuilds.OpeningOneGateCore_DragoonFirst: _*))
  private class ImplementEarly1015GateGoon  extends FirstFiveMinutes(new Build(ProtossBuilds.OpeningTwoGate1015Dragoons: _*))
  private class ImplementEarlyDTExpand      extends FirstFiveMinutes(new Build(ProtossBuilds.OpeningDTExpand: _*))
  
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
      new UnitsAtLeast(6, UnitMatchType(Protoss.Dragoon)),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))
    ),
    new RequireMiningBases(3))
  
  private class FulfillMidgameTech extends Build(
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUpgrade(Protoss.ZealotSpeed))
  
  //////////////////////////
  // Late-game strategies //
  //////////////////////////
  
  private class ImplementLateCarriers extends OnMiningBases(3,
    new Build(
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(3, Protoss.Stargate),
      RequestUpgrade(Protoss.AirDamage, 1),
      RequestUpgrade(Protoss.CarrierCapacity),
      RequestUpgrade(Protoss.AirDamage, 2),
      RequestUpgrade(Protoss.AirDamage, 3)))
  
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
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
    // Early game
    new RequireMiningBases(1),
    new Employ(Early14Nexus,      new ImplementEarly14Nexus),
    new Employ(Early1GateRange,   new ImplementEarly1GateRange),
    new Employ(Early1015GateGoon, new ImplementEarly1015GateGoon),
    new Employ(EarlyDTExpand,     new ImplementEarlyDTExpand),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new ProtossVsTerranIdeas.RespondToBioAllInWithReavers,
    new RequireMiningBases(2),
    new FulfillEarlyTech,
    
    // Mid game
    new MatchMiningBases(1),
    new TakeThirdBaseSafely,
    new OnMiningBases(2,
      new Parallel(
        new ProtossVsTerranIdeas.RespondToBioWithReavers,
        new BuildAssimilators,
        new IfCloakedThreats_Observers,
        new FulfillMidgameTech)),
    
    // Late game
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors), // We have a habit of getting this tech too soon and dying
      new Parallel(
        new Employ(LateArbiters, new ImplementLateArbiters),
        new Employ(LateCarriers, new ImplementLateCarriers),
        new OnThreeBases_WeaponsUpgrades)),
    
    // Units
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new TrainContinuously(Protoss.Carrier)),
    new IfNoDetection_DarkTemplar,
    new If(
      new EnemyBio,
      new TrainContinuously(Protoss.Reaver, 2)),
    new TrainContinuously(Protoss.Arbiter, 3),
    new TrainContinuously(Protoss.Observer, 3),
    new ProtossVsTerranIdeas.BuildDragoonsUntilWeHaveZealotSpeed,
    
    // Luxuries
    new Build(RequestAtLeast(5, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Employ(LateCarriers, new ImplementLateCarriers),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(15, Protoss.Gateway)),
    new CompleteUpgrades,
    
    // Tactics
    new ScoutAt(14),
    new ScoutExpansionsAt(100),
    new ControlMap,
    new ConsiderAttacking
  ))
}

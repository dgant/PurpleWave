package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchDroppable, UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.{Employ, Employing}
import Planning.Plans.Information.Reactive.{EnemyBio, EnemyBioAllIn}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, RequireBareMinimum}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, _}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT._

class ProtossVsTerran extends Parallel {
  
  description.set("Protoss vs. Terran")
  
  ///////////////////////////
  // Early-game strategies //
  ///////////////////////////
  
  private class FulfillEarlyTech extends Build(
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange))
  
  /////////////////////////
  // Mid-game strategies //
  /////////////////////////
  
  private class ConsiderTakingSecondBase extends If(
    new Or(
      new UnitsAtLeast(1, UnitMatchWarriors, complete = false),
      new Employing(PvTEarly14Nexus)),
    new RequireMiningBases(2))
  
  private class ConsiderTakingThirdBase extends If(
    new Or(
      new UnitsAtLeast(4, UnitMatchWarriors, complete = false),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver))),
    new RequireMiningBases(3))
  
  private class FulfillMidgameTech extends Build(
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.RoboticsFacility),
    RequestAtLeast(1,   Protoss.Observatory),
    RequestAtLeast(4,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.RoboticsSupportBay),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUpgrade(Protoss.ZealotSpeed),
    RequestAtLeast(1,   Protoss.Forge))
  
  //////////////////////////
  // Late-game strategies //
  //////////////////////////
  
  private class ImplementLateCarriers extends OnMiningBases(3,
    new Parallel(
      new Build(
        RequestAtLeast(6, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Stargate),
        RequestAtLeast(1, Protoss.FleetBeacon),
        RequestAtLeast(2, Protoss.Stargate),
        RequestUpgrade(Protoss.AirDamage, 1),
        RequestUpgrade(Protoss.CarrierCapacity)),
      new If(
        new EnemyBio,
        new Parallel(
          new UpgradeContinuously(Protoss.AirArmor),
          new Build(RequestAtLeast(2, Protoss.CyberneticsCore)),
          new UpgradeContinuously(Protoss.AirDamage)
        ),
        new Parallel(
          new UpgradeContinuously(Protoss.AirDamage),
          new Build(RequestAtLeast(2, Protoss.CyberneticsCore)),
          new UpgradeContinuously(Protoss.AirArmor)
        ))))
  
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
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)))
  
  class TrainZealotsOrDragoons extends If(
    new And(
      new HaveUpgrade(Protoss.ZealotSpeed, withinFrames = Protoss.Zealot.buildFrames),
      new Or(
        new UnitsAtLeast(18, UnitMatchType(Protoss.Dragoon)),
        new Check(() => With.self.gas * 5 < With.self.minerals))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon))
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
  
    ////////////////
    // Early game //
    ////////////////
    
    new RequireBareMinimum,
    new Employ(PvTEarly14Nexus,       new FirstEightMinutes(new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*))),
    new Employ(PvTEarly1GateRange,    new FirstEightMinutes(new Build(ProtossBuilds.Opening_10Gate11Gas13Core: _*))),
    new Employ(PvTEarly1GateReaver,   new FirstEightMinutes(new Build(ProtossBuilds.Opening_10Gate11Gas13Core: _*))),
    new Employ(PvTEarly2GateObs,      new FirstEightMinutes(new Build(ProtossBuilds.Opening_10Gate11Gas13Core: _*))),
    new Employ(PvTEarly1015GateGoon,  new FirstEightMinutes(new Build(ProtossBuilds.Opening_10Gate15GateDragoons: _*))),
    new Employ(PvTEarly1015GateDT,    new FirstEightMinutes(new Build(ProtossBuilds.Opening_10Gate15GateDragoons: _*))),
    new Employ(PvTEarlyDTExpand,      new FirstEightMinutes(new Build(ProtossBuilds.OpeningDTExpand: _*))),
    
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    
    ///////////////////
    // One-base tech //
    ///////////////////
    
    new IfCloakedThreats_Observers,
    new FirstEightMinutes(
      new Parallel(
        new Employ(PvTEarly1GateRange, new Parallel(
          new Build(
            RequestAtLeast(1, Protoss.Dragoon),
            RequestAtLeast(2, Protoss.Nexus)))),
        new Employ(PvTEarly1GateReaver, new Parallel(
          new TrainContinuously(Protoss.Reaver, 1),
          new Build(
            RequestAtLeast(1, Protoss.Gateway),
            RequestAtLeast(1, Protoss.CyberneticsCore),
            RequestAtLeast(1, Protoss.RoboticsFacility)),
          new TrainZealotsOrDragoons,
          new Build(
            RequestAtLeast(1, Protoss.Shuttle),
            RequestAtLeast(1, Protoss.RoboticsSupportBay),
            RequestAtLeast(1, Protoss.Reaver),
            RequestUpgrade(Protoss.DragoonRange))
        )),
        new Employ(PvTEarly1015GateDT, new Parallel(
          new TrainContinuously(Protoss.DarkTemplar, 4),
          new TrainZealotsOrDragoons,
          new Build(
            RequestAtLeast(1, Protoss.CitadelOfAdun),
            RequestAtLeast(1, Protoss.TemplarArchives)))),
        new Employ(PvTEarly2GateObs, new Parallel(
          new TrainZealotsOrDragoons,
          new TrainContinuously(Protoss.Observer, 1),
          new Build(
            RequestAtLeast(1, Protoss.Gateway),
            RequestAtLeast(1, Protoss.CyberneticsCore),
            RequestUpgrade(Protoss.DragoonRange),
            RequestAtLeast(1, Protoss.RoboticsFacility),
            RequestAtLeast(2, Protoss.Gateway),
            RequestAtLeast(1, Protoss.Observatory))
        )))),
    new If(
      new EnemyBioAllIn,
      new Build(ProtossBuilds.TechReavers: _*)),
    
    //////////////////////
    // Two base midgame //
    //////////////////////
  
    // Make sure we get an early Dragoon for Vulture defense
    new Trigger(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Dragoon)),
      initialBefore = new If(
        new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore)),
        new Build(RequestAtLeast(1, Protoss.Dragoon)))),
    
    new FulfillEarlyTech,
    new BuildGasPumps,
  
    // Mid game
     new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.HighTemplar), complete = false),
      new Build(RequestTech(Protoss.PsionicStorm))),
  
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Arbiter), complete = false),
      new Build(RequestTech(Protoss.Stasis))),
  
    new ConsiderTakingSecondBase,
    new ConsiderTakingThirdBase,
    
    new OnMiningBases(2,
      new Parallel(
        new If(
          new EnemyBio,
          new Build(ProtossBuilds.TechReavers: _*)),
        new If(
          new UnitsAtLeast(8, UnitMatchWarriors, complete = true),
          new FulfillMidgameTech))),
    
    new OnMiningBases(3, new BuildCannonsAtExpansions(1)),
    
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
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Arbiter, 3),
    new If(
      new Or(
        new EnemyHasShown(Terran.SpiderMine),
        new EnemyHasTech(Terran.WraithCloak)),
      new TrainContinuously(Protoss.Observer, 3),
      new TrainContinuously(Protoss.Observer, 1)
    ),
    new If(
      new Or(
        new UnitsAtLeast(2, UnitMatchDroppable),
        new UnitsAtLeast(2, UnitMatchType(Protoss.Reaver))),
      new Parallel(
        new Build(RequestAtLeast(1, Protoss.Shuttle)),
        new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay)),
        new Build(RequestUpgrade(Protoss.ShuttleSpeed)),
        new Build(RequestAtLeast(2, Protoss.Shuttle)))
    ),
    new TrainZealotsOrDragoons,
    
    // Luxuries
    new Build(RequestAtLeast(2, Protoss.Gateway)),
    new Build(RequestAtLeast(1, Protoss.RoboticsFacility)),
    new Build(RequestAtLeast(3, Protoss.Gateway)),
    new Build(RequestAtLeast(1, Protoss.Observatory)),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(6, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(15, Protoss.Gateway)),
    new CompleteUpgrades,
    
    // Tactics
    new ScoutAt(16),
    new ScoutExpansionsAt(100),
    new ClearBurrowedBlockers,
    new DropAttack,
    new Attack { attackers.get.unitMatcher.set(UnitMatchType(Protoss.DarkTemplar)) },
    new DefendZones,
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
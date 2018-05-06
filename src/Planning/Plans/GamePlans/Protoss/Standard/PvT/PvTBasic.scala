package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound.{Or, Parallel, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainWorkersContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBio}
import Planning.Plans.Predicates.{Employing, Never, SafeAtHome, SafeToAttack}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._

class PvTBasic extends GameplanModeTemplate {
  override val activationCriteria     = new Employing(PvT13Nexus, PvT21Nexus, PvTDTExpand, PvT2BaseCarrier, PvT2BaseArbiter, PvT3BaseCarrier, PvT3BaseArbiter)
  override val completionCriteria     = new Never
  override val buildOrder             = ProtossBuilds.OpeningDTExpand
  override val defaultWorkerPlan      = new TrainWorkersContinuously(oversaturate = true)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  
  override def defaultScoutPlan: Plan = new Parallel(
    new If(new Employing(PvT13Nexus),       new ScoutOn(Protoss.Nexus, quantity = 2)),
    new If(new Employing(PvT21Nexus),       new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT2GateObserver), new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvTDTExpand),      new ScoutOn(Protoss.Pylon)))
  
  override val defaultAttackPlan = new PvTIdeas.AttackRespectingMines
  
  override def emergencyPlans: Seq[Plan] = Vector(new PvTIdeas.EmergencyBuilds)
  
  override def defaultBuildOrder: Plan = new Parallel(
    new If(new Employing(PvT13Nexus),       new BuildOrder(ProtossBuilds.Opening13Nexus_NoZealot_TwoGateways: _*)),
    new If(new Employing(PvT21Nexus),       new BuildOrder(ProtossBuilds.Opening21Nexus_Robo: _*)),
    new If(new Employing(PvT2GateObserver), new BuildOrder(ProtossBuilds.Opening2GateObserver: _*)),
    new If(new Employing(PvTDTExpand),      new BuildOrder(ProtossBuilds.OpeningDTExpand: _*)))
  
  class EmployingThreeBase extends Employing(PvT3BaseCarrier, PvT3BaseArbiter)
  class EmployingCarriers extends Employing(PvT2BaseCarrier, PvT3BaseCarrier)
  class EmployingArbiters extends Employing(PvT2BaseArbiter, PvT3BaseArbiter)
  
  class NeedObservers extends Or(
    new EnemyHasShownCloakedThreat,
    new EnemyUnitsAtLeast(2, Terran.Vulture),
    new EnemyUnitsAtLeast(2, Terran.Factory))
  
  class ReadyForThirdBase extends And(
    new Or(
      new EmployingThreeBase,
      new Latch(new UnitsAtLeast(1, Protoss.Arbiter, complete = true)),
      new Latch(new UnitsAtLeast(4, Protoss.Carrier))),
    new Or(
      new EnemyBasesAtLeast(2),
      new And(
        new SafeAtHome,
        new UnitsAtLeast(8, UnitMatchWarriors))),
    new Or(
      new Not(new NeedObservers),
      new UnitsAtLeast(1, Protoss.Observer, complete = true)))
  
  class ReadyForFourthBase extends And(
    new ReadyForThirdBase,
    new SafeToAttack,
    new Or(
      new Latch(new UnitsAtLeast(6, Protoss.Carrier, complete = true)),
      new Latch(new UnitsAtLeast(3, Protoss.Arbiter, complete = true))))
  
  class BasicTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Gateway),
      RequestUpgrade(Protoss.DragoonRange)))
  
  class ObserverTech extends Parallel(
    new BasicTech,
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)),
    new If(
      new And(
        new EnemyUnitsAtLeast(3, Terran.SpiderMine),
        new Or(
          new MiningBasesAtLeast(3),
          new EmployingThreeBase)),
      new UpgradeContinuously(Protoss.ObserverSpeed)))
  
  class CriticalUpgrades extends Parallel(
    new If(
      new UnitsAtLeast(2, Protoss.Carrier),
      new Parallel(
        new If(
          new UnitsAtLeast(2, Protoss.CyberneticsCore),
          new Parallel(
            new UpgradeContinuously(Protoss.AirArmor),
            new UpgradeContinuously(Protoss.AirDamage)),
          new If(
            new And(
              new Not(new UpgradeComplete(Protoss.AirArmor, 3)),
              new Or(
                new UpgradeComplete(Protoss.AirDamage, 3),
                new EnemyBio)),
            new UpgradeContinuously(Protoss.AirArmor),
            new UpgradeContinuously(Protoss.AirDamage))),
        new UpgradeContinuously(Protoss.CarrierCapacity))),
    new UpgradeContinuously(Protoss.ArbiterEnergy),
    new If(
      new UnitsAtLeast(1, Protoss.Arbiter, complete = true),
      new Build(RequestTech(Protoss.Stasis))),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(RequestTech(Protoss.PsionicStorm))),
    new If(
      new UnitsAtLeast(4, Protoss.Zealot),
      new UpgradeContinuously(Protoss.ZealotSpeed)),
    new If(
      new UnitsAtLeast(8, UnitMatchWarriors),
      new UpgradeContinuously(Protoss.GroundDamage)))
  
  class LateGameTech extends Parallel(
    new BuildGasPumps,
    new If(
      new EmployingCarriers,
      new Parallel(
        new If(
          new EnemyBio,
          new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
        new Build(
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.FleetBeacon)),
        new If(
          new UnitsAtLeast(1, Protoss.FleetBeacon),
          new Build(RequestAtLeast(2, Protoss.Stargate))))),
    new If(
      new EmployingArbiters,
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(1, Protoss.ArbiterTribunal)))))
  
  override val buildPlans = Vector(
    
    new RequireMiningBases(2),
    new If(
      new ReadyForThirdBase,
      new RequireMiningBases(3)),
    new If(
      new ReadyForFourthBase,
      new RequireMiningBases(4)),
    new BuildCannonsAtExpansions(2),
  
    new BasicTech,
    new CriticalUpgrades,
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new PvTIdeas.TrainArmy,
        new ObserverTech),
      new Parallel(
        new ObserverTech,
        new LateGameTech)),
    
    new Build(RequestAtLeast(6, Protoss.Gateway)),
    
    new If(
      new OnGasPumps(3),
      new Parallel(
        new If(
          new EmployingArbiters,
          new Build(RequestAtLeast(2, Protoss.Stargate))),
        new If(
          new EmployingCarriers,
          new Parallel(
            new Build(RequestAtLeast(3, Protoss.Stargate)),
            new UpgradeContinuously(Protoss.AirArmor),
            new UpgradeContinuously(Protoss.AirDamage))),
        new Build(RequestAtLeast(2, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundArmor),
        new Build(RequestAtLeast(1, Protoss.TemplarArchives)))),
  
    new If(
      new OnGasPumps(4),
      new If(
        new EmployingCarriers,
        new Build(RequestAtLeast(2, Protoss.CyberneticsCore)))),
  
    new RequireMiningBases(3),
    new Build(RequestAtLeast(1, Protoss.CitadelOfAdun)),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(24, Protoss.Gateway))
  )
}


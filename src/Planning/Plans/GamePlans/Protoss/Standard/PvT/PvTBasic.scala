package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Aggression, EjectScout}
import Planning.Plans.Compound.{Or, Parallel, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTIdeas.TrainMinimumDragoons
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Scouting.{Scout, ScoutCleared, ScoutOn}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._

class PvTBasic extends GameplanTemplate {
  override val activationCriteria = new Employing(
    PvT13Nexus,
    PvT21Nexus,
    PvT23Nexus,
    PvT28Nexus,
    PvT2GateRangeExpand,
    PvTDTExpand,
    PvT1GateRobo,
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT3BaseCarrier,
    PvT3BaseArbiter)

  override def aggressionPlan: Plan =
    new If(
      new And(
        new EmployingCarriers,
        new UnitsAtLeast(1, Protoss.Stargate),
        new UnitsAtMost(32, Protoss.Interceptor)),
      new Aggression(0.8),
      new If(
        new And(
          new EmployingArbiters,
          new UnitsAtLeast(1, Protoss.Stargate),
          new UnitsAtMost(1, Protoss.Arbiter, complete = true)),
        new Aggression(0.9),
        new Aggression(1.0)))

  override val meldArchonsAt: Int = 25

  override def scoutPlan: Plan = new Parallel(
    new If(new Employing(PvT13Nexus),           new ScoutOn(Protoss.Nexus, quantity = 2)),
    new If(new Employing(PvT21Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT23Nexus),           new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT28Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT2GateRangeExpand),  new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT1GateRobo),         new ScoutOn(Protoss.CyberneticsCore)),
    new If(new Employing(PvT2GateObserver),     new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT1015DT),            new If(new UpgradeStarted(Protoss.DragoonRange), new Scout)),
    new If(new Employing(PvTDTExpand),          new ScoutOn(Protoss.CyberneticsCore)))

  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val attackPlan = new Parallel(
    new If(
      new Or(
        new Not(new Employing(PvTDTExpand, PvT1GateRobo)),
        new Latch(new UnitsAtLeast(1, UnitMatchOr(Protoss.DarkTemplar, Protoss.Reaver), complete = true)),
        new UpgradeStarted(Protoss.DragoonRange)),
      new PvTIdeas.AttackSafely))

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvTIdeas.ReactToBBS,
    new PvTIdeas.ReactToWorkerRush,
    new If(
      new Employing(PvT13Nexus, PvT21Nexus, PvT23Nexus, PvT28Nexus),
      new PvTIdeas.ReactTo2Fac))

  override def buildOrderPlan: Plan = new Parallel(
    new If(new Employing(PvT13Nexus),           new BuildOrder(ProtossBuilds.PvT13Nexus_GateCoreGateZ: _*)),
    new If(new Employing(PvT21Nexus),           new BuildOrder(ProtossBuilds.PvT21Nexus: _*)),
    new If(new Employing(PvT23Nexus),           new BuildOrder(ProtossBuilds.PvT23Nexus: _*)),
    new If(new Employing(PvT28Nexus),           new BuildOrder(ProtossBuilds.PvT28Nexus: _*)),
    new If(new Employing(PvT2GateRangeExpand),  new BuildOrder(ProtossBuilds.PvT2GateRangeExpand: _*)),
    new If(new Employing(PvT2GateObserver),     new BuildOrder(ProtossBuilds.PvT2GateObs: _*)),
    new If(new Employing(PvT1015DT),            new BuildOrder(ProtossBuilds.PvT1015GateGoonDT: _*)),
    new If(new Employing(PvT1GateRobo),         new BuildOrder(ProtossBuilds.PvT1GateReaver: _*)),
    // DT expand, but don't build a Citadel in the enemy's face
    new If(new Employing(PvTDTExpand), new Parallel(
      new BuildOrder(ProtossBuilds.PvTDTExpand_BeforeCitadel: _*),
      new Trigger(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new BuildOrder(ProtossBuilds.PvTDTExpand_WithCitadel: _*),
        new Trigger(
          new And(
            new UnitsAtLeast(2, Protoss.Nexus),
            new Not(new ScoutCleared)),
          new BuildOrder(ProtossBuilds.PvTDTExpand_WithoutCitadel: _*),
          new If(
            new ScoutCleared,
            new BuildOrder(ProtossBuilds.PvTDTExpand_WithCitadel: _*),
            new BuildOrder(ProtossBuilds.PvTDTExpand_WithoutCitadel: _*)))))))

  class EmployingTwoBase    extends Employing(PvT2BaseCarrier, PvT2BaseArbiter)
  class EmployingThreeBase  extends Employing(PvT3BaseCarrier, PvT3BaseArbiter)
  class EmployingCarriers   extends Employing(PvT2BaseCarrier, PvT3BaseCarrier)
  class EmployingArbiters   extends Or(new Employing(PvT2BaseArbiter, PvT3BaseArbiter), new Check(() => With.units.countEnemy(Terran.Goliath) > Math.max(10, With.units.countOurs(Protoss.Interceptor) / 3)))

  class NeedObservers extends Or(
    new EnemyHasShownCloakedThreat,
    new Latch(new EnemiesAtLeast(4, Terran.Vulture)))

  class PreparedForBio extends Latch(
    new Or(new UnitsAtLeast(1, Protoss.Reaver), new TechComplete(Protoss.PsionicStorm)))

  class ReadyForThirdBase extends And(
    new Or(
      new EmployingThreeBase,
      new And(new EnemyBio, new PreparedForBio),
      new Latch(new UnitsAtLeast(1, Protoss.Arbiter)),
      new Latch(new UnitsAtLeast(4, Protoss.Carrier))),
    new Or(
      new EnemyBasesAtLeast(2),
      new And(new EnemyBio, new PreparedForBio),
      new And(new SafeAtHome, new UnitsAtLeast(6, UnitMatchWarriors))),
    new Or(
      new Not(new NeedObservers),
      new UnitsAtLeast(1, Protoss.Observer, complete = true)))

  class ReadyForFourthBase extends And(
    new ReadyForThirdBase,
    new SafeToMoveOut,
    new Or(
      new EnemyBasesAtLeast(3),
      new And(new EnemyBio, new PreparedForBio),
      new Latch(new UnitsAtLeast(2, Protoss.Arbiter, complete = true)),
      new Latch(new UnitsAtLeast(4, Protoss.Carrier, complete = true))))

  class BasicTech extends Parallel(
    new BuildOrder(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(1, Protoss.Dragoon),
      Get(Protoss.DragoonRange)))

  class ObserverTech extends Parallel(
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory)),
    new If(
      new And(
        new EnemiesAtLeast(3, Terran.SpiderMine),
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
          new FlipIf(
            new EnemyBio,
            new UpgradeContinuously(Protoss.AirDamage),
            new UpgradeContinuously(Protoss.AirArmor)),
          new If(
            new And(
              new Not(new UpgradeComplete(Protoss.AirArmor, 3)),
              new Or(
                new UpgradeComplete(Protoss.AirDamage, 3),
                new EnemyBio)),
            new UpgradeContinuously(Protoss.AirArmor),
            new UpgradeContinuously(Protoss.AirDamage))),
        new UpgradeContinuously(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(1, Protoss.ArbiterTribunal), new Build(Get(Protoss.ArbiterEnergy))),
    new If(new UpgradeComplete(Protoss.ArbiterEnergy), new Build(Get(Protoss.Stasis))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle), new UpgradeContinuously(Protoss.ShuttleSpeed)),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(4, Protoss.Zealot), new UpgradeContinuously(Protoss.ZealotSpeed)),

    // Get upgrades with Arbiter builds, vs. Bio, or when maxed on air upgrades
    // Double-spin, or prioritize armor vs. bio
    new If(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new Or(
          new EmployingArbiters,
          new EnemyBio,
          new And(
            new UpgradeComplete(Protoss.AirDamage, 2, Protoss.AirDamage.upgradeFrames(2)),
            new UpgradeComplete(Protoss.AirArmor, 2, Protoss.AirArmor.upgradeFrames(2))))),
      new If(
        new UnitsAtLeast(2, Protoss.Forge),
        new FlipIf(
          new EnemyBio,
          new UpgradeContinuously(Protoss.GroundDamage),
          new UpgradeContinuously(Protoss.GroundArmor)),
        new If(
          new And(
            new Not(new UpgradeComplete(Protoss.GroundArmor, 3)),
            new Or(
              new UpgradeComplete(Protoss.GroundDamage, 3),
              new EnemyBio)),
          new UpgradeContinuously(Protoss.GroundArmor),
          new UpgradeContinuously(Protoss.GroundDamage)))))

  class LateGameTech extends Parallel(
    new BuildGasPumps,
    new If(
      new EnemyBio,
      // Bio reaction
      new Parallel(
        new If(
          new And(
            new UnitsExactly(0, Protoss.TemplarArchives),
            new UnitsAtLeast(1, Protoss.RoboticsFacility)),
          new Build(
            Get(Protoss.RoboticsSupportBay),
            Get(5, Protoss.Gateway))),
        new Build(
            Get(Protoss.CitadelOfAdun),
            Get(Protoss.TemplarArchives),
            Get(5, Protoss.Gateway),
            Get(Protoss.HighTemplarEnergy),
            Get(2, Protoss.Forge))),
      // No bio? Normal game plan
      new Parallel(
        new If(
          new Or(
            new EnemyStrategy(With.fingerprints.twoFac, With.fingerprints.twoFacVultures, With.fingerprints.threeFac, With.fingerprints.threeFacVultures),
            new EmployingThreeBase),
          new Build(
            Get(2, Protoss.Gateway),
            Get(Protoss.RoboticsFacility),
            Get(Protoss.Observatory),
            Get(3, Protoss.Gateway))),
        new If(
          // Time for Carrier tech?
          new And(
            new EmployingCarriers,
            new Or(
              new EmployingTwoBase,
              new BasesAtLeast(3)),
            new Or(
              new ScoutCleared,
              new FrameAtLeast(GameTime(4, 45)()))),
          new Parallel(
            new If(
              new BasesAtLeast(3),
              new Build(
                Get(Protoss.Stargate),
                Get(Protoss.FleetBeacon)),
              new Build(
                Get(Protoss.Stargate),
                Get(Protoss.CitadelOfAdun),
                Get(Protoss.FleetBeacon),
                Get(Protoss.ZealotSpeed))),
            new UpgradeContinuously(Protoss.AirDamage),
            new If(
              new UnitsAtLeast(1, Protoss.FleetBeacon),
              new If(
                new And(
                  new MiningBasesAtLeast(3),
                  // TODO: Document: why this list? I think it was to avoid going too Carrier-heavy after low-econ openers
                  new Employing(PvT13Nexus, PvT21Nexus, PvT23Nexus, PvT28Nexus, PvT1015Expand, PvT1015TripleExpand)),
                new Build(
                  Get(3, Protoss.Stargate),
                  Get(3, Protoss.Carrier),
                  Get(6, Protoss.Gateway)),
                new Build(
                  Get(2, Protoss.Stargate),
                  Get(2, Protoss.Carrier),
                  Get(4, Protoss.Gateway)))))),
        new If(
          new Or(
            new EmployingArbiters,
            new And(
              new UnitsAtLeast(8, Protoss.Carrier),
              new GasPumpsAtLeast(3),
              new MiningBasesAtLeast(3))),
          new Parallel(
            new Build(
              Get(Protoss.CitadelOfAdun),
              Get(Protoss.TemplarArchives),
              Get(Protoss.Stargate),
              Get(Protoss.ArbiterTribunal),
              Get(5, Protoss.Gateway)))))))
  
  class BonusTech extends Parallel(
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.ZealotSpeed)),
    new If(
      new And(
        new EmployingArbiters,
        new GasPumpsAtLeast(3)),
      new Parallel(
        new Build(
          Get(7, Protoss.Gateway),
          Get(2, Protoss.Stargate),
          Get(2, Protoss.Forge)),
        new ObserverTech)),
    new If(
      new And(
        new EmployingCarriers,
        new GasPumpsAtLeast(3)),
      new Build(
        Get(2, Protoss.CyberneticsCore),
        Get(3, Protoss.Stargate))))

  override val buildPlans = Vector(
    new EjectScout,
    new RequireMiningBases(2),
    new BasicTech,
    new If(new EmployingTwoBase,    new BuildGasPumps),
    new If(new ReadyForThirdBase,   new If(new UnitsAtMost(1, Protoss.Gateway), new Pump(Protoss.Dragoon)), new RequireMiningBases(3)),
    new If(new ReadyForFourthBase,  new RequireMiningBases(4)),
    new If(
      new UnitsAtLeast(3, Protoss.Gateway),
      new BuildCannonsAtExpansions(2),
      new If(
        new BasesAtLeast(3),
        new BuildCannonsAtNatural(1))),

    new CriticalUpgrades,
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new If(
          new And(
            new UnitsAtLeast(4, Protoss.Carrier),
            new BasesAtLeast(3)),
          new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
        new PvTIdeas.TrainArmy,
        new If(new EnemyHasShown(Terran.Wraith), new ObserverTech)),
      new Parallel(
        new TrainMinimumDragoons,
        new LateGameTech)),
  
    new RequireMiningBases(3),
    new BonusTech,
    new Build(Get(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(Get(16, Protoss.Gateway)),
    new Build(Get(3, Protoss.Stargate)),
    new RequireMiningBases(5),
    new Build(Get(24, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.HighTemplarEnergy),
    new UpgradeContinuously(Protoss.AirDamage),
    new RequireMiningBases(6),
  )
}


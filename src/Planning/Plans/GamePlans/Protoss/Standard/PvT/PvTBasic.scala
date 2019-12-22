package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Aggression, EjectScout, Hunt}
import Planning.Plans.Compound.{Or, Parallel, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTIdeas.PvTAttack
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Scouting.{MonitorBases, Scout, ScoutCleared, ScoutOn}
import Planning.Predicates.Compound._
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyIsRandom, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchOr
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._

class PvTBasic extends GameplanTemplate {
  override val activationCriteria = new Employing(
    PvT13Nexus,
    PvT21Nexus,
    PvT23Nexus,
    PvT28Nexus,
    PvT32Nexus,
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

  override val meldArchonsAt: Int = -1

  override def scoutPlan: Plan = new Parallel(
    new If(new EnemyIsRandom,                   new ScoutOn(Protoss.Pylon)), // Continue scouting from a PvR opening
    new If(new Employing(PvT13Nexus),           new ScoutOn(Protoss.Nexus, quantity = 2)),
    new If(new Employing(PvT21Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT23Nexus),           new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT28Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT32Nexus),           new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT2GateRangeExpand),  new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT1GateRobo),         new ScoutOn(Protoss.CyberneticsCore)),
    new If(new Employing(PvT2GateObserver),     new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT1015DT),            new If(new UpgradeStarted(Protoss.DragoonRange), new Scout)),
    new If(new Employing(PvTDTExpand),          new ScoutOn(Protoss.CyberneticsCore)))

  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val attackPlan = new Parallel(
    new Hunt(Protoss.DarkArchon, Terran.ScienceVessel),
    // COG 2019 hack -- Don't get locked in our base
    new If(
      new And(
        new Latch(new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true)),
        new BasesAtMost(1)),
      new PvTAttack),
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
    new If(new Employing(PvT32Nexus),           new BuildOrder(ProtossBuilds.PvT32Nexus: _*)),
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

  class EmployingThreeBase  extends And(new Not(new EnemyStrategy(With.fingerprints.bio)), new Or(new Sticky(new MiningBasesAtLeast(3)), new Employing(PvT3BaseCarrier, PvT3BaseArbiter)))
  class EmployingTwoBase    extends Not(new EmployingThreeBase)
  class CarriersCountered   extends Check(() => With.units.countEnemy(Terran.Goliath) > Math.max(10, With.units.countOurs(Protoss.Interceptor) / 1.8))
  class EmployingCarriers   extends And(new Employing(PvT2BaseCarrier, PvT3BaseCarrier), new Not(new CarriersCountered))
  class EmployingArbiters   extends Or( new Employing(PvT2BaseArbiter, PvT3BaseArbiter),         new CarriersCountered)
  class WantObservers       extends Or(new EnemyStrategy(With.fingerprints.twoFac), new EmployingThreeBase, new EnemyHasShownWraithCloak)
  class PreparedForBio      extends Latch(new Or(new UnitsAtLeast(1, Protoss.Reaver), new TechComplete(Protoss.PsionicStorm)))

  class ReadyForThirdBase extends And(
    // Don't expand if unprepared for bio
    new Or(
      new Not(new EnemyStrategy(With.fingerprints.bio)),
      new PreparedForBio),
    // 2-base builds: Expand once our tech is in swing
    // 3-base builds: Expand the moment we're safe
    new Or(
      new Latch(new UnitsAtLeast(1, Protoss.Arbiter)),
      new And(
        new Latch(new UnitsAtLeast(2, Protoss.Carrier)),
        new EnemyStrategy(With.fingerprints.oneRaxFE),
        new EnemyStrategy(With.fingerprints.siegeExpand)),
      new Latch(new UnitsAtLeast(4, Protoss.Carrier)),
      new And(
        new EmployingThreeBase,
        new Or(
          new EnemyBasesAtLeast(3),
          new EnemiesAtLeast(3, Terran.CommandCenter),
          new And(
            new UnitsAtLeast(3, Protoss.Gateway, complete = true),
            new SafeAtHome)))))

  class ReadyForFourthBase extends And(
    new ReadyForThirdBase,
    new SafeToMoveOut,
    new Or(
      new UnitsAtLeast(3, Protoss.Nexus, complete = true),
      new And(new EnemyStrategy(With.fingerprints.bio), new PreparedForBio),
      new Latch(new UnitsAtLeast(2, Protoss.Arbiter, complete = true)),
      new Latch(new UnitsAtLeast(6, Protoss.Carrier, complete = true))))

  class ReadyForFifthBase extends And(
    new ReadyForFourthBase,
    new SafeToMoveOut,
    new UnitsAtLeast(4, Protoss.Nexus, complete = true))

  class BasicOpening extends Parallel(
    new BuildOrder(
      Get(Protoss.Pylon),
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Dragoon),
      Get(Protoss.DragoonRange)))

  class GetObserverTech extends Parallel(
    new BuildOrder(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer)),
    new If(
      new And(
        new EnemiesAtLeast(3, Terran.SpiderMine),
        new GasPumpsAtLeast(3)),
      new UpgradeContinuously(Protoss.ObserverSpeed)))

  class HighPriorityTech extends Parallel(
    new If(new EnemyHasShown(Terran.Wraith), new GetObserverTech),
    new If(
      new Or(
        new UnitsAtLeast(1, Protoss.HighTemplar),
        new GasPumpsAtLeast(1200),
        new And(
          new EmployingCarriers,
          new Or(
            new EnemiesAtLeast(8, Terran.Goliath),
            new GasPumpsAtLeast(5)))),
      new Build(Get(Protoss.TemplarArchives), Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),           new UpgradeContinuously(Protoss.ZealotSpeed)),
    new If(new UnitsAtLeast(1, Protoss.ArbiterTribunal),  new Build(Get(Protoss.ArbiterEnergy))),
    new If(
      new And(
        new EmployingCarriers,
        new UnitsAtLeast(1, Protoss.Stargate)),
      new Parallel(
        new If(
          new UnitsAtLeast(2, Protoss.CyberneticsCore),
          new FlipIf(
            new EnemyStrategy(With.fingerprints.bio),
            new UpgradeContinuously(Protoss.AirDamage),
            new UpgradeContinuously(Protoss.AirArmor)),
          new If(
            new EnemyStrategy(With.fingerprints.bio),
            new Parallel(
              new UpgradeContinuously(Protoss.AirArmor),
              new If(new UpgradeComplete(Protoss.AirArmor, 3), new UpgradeContinuously(Protoss.AirDamage))),
            new Parallel(
              new UpgradeContinuously(Protoss.AirDamage),
              new If(new UpgradeComplete(Protoss.AirDamage, 3), new UpgradeContinuously(Protoss.AirArmor))))),
        new UpgradeContinuously(Protoss.CarrierCapacity))),

    new If(
      new Or(
        new MineralOnlyBase,
        new And(new MiningBasesAtLeast(3), new UnitsAtLeast(4, Protoss.Carrier)),
        new And(new MiningBasesAtLeast(3), new UnitsAtLeast(15, Protoss.Dragoon))),
      new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new If(new UpgradeComplete(Protoss.ArbiterEnergy), new Build(Get(Protoss.Stasis))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle), new UpgradeContinuously(Protoss.ShuttleSpeed)))

  class LowPriorityUpgrades extends Parallel(
    new If(new And(new EmployingArbiters, new GasPumpsAtLeast(3)), new Build(Get(2, Protoss.Stargate), Get(2, Protoss.Forge))),
    new If(new And(new EmployingCarriers, new GasPumpsAtLeast(3)), new Build(Get(2, Protoss.CyberneticsCore))),
    new If(new EmployingCarriers, new UpgradeContinuously(Protoss.Shields, 1)),

    // Get upgrades with Arbiter builds, vs. Bio, or when maxed on air upgrades
    // Double-spin, or prioritize armor vs. bio
    new If(
      new Or(
        new EnemyStrategy(With.fingerprints.bio),
        new EmployingArbiters,
        new And(
          new UpgradeComplete(Protoss.AirDamage, 2, Protoss.AirDamage.upgradeFrames(2)),
          new UpgradeComplete(Protoss.AirArmor, 2, Protoss.AirArmor.upgradeFrames(2)))),
      new If(
        new UnitsAtLeast(2, Protoss.Forge),
        new FlipIf(
          new EnemyStrategy(With.fingerprints.bio),
          new UpgradeContinuously(Protoss.GroundDamage),
          new UpgradeContinuously(Protoss.GroundArmor)),
        new If(
          new EnemyStrategy(With.fingerprints.bio),
          new Parallel(
            new UpgradeContinuously(Protoss.GroundArmor),
            new If(new UpgradeComplete(Protoss.GroundArmor, 3), new UpgradeContinuously(Protoss.GroundDamage))),
          new Parallel(
            new UpgradeContinuously(Protoss.GroundDamage),
            new If(new UpgradeComplete(Protoss.GroundDamage, 3), new UpgradeContinuously(Protoss.GroundArmor)))))))

  class ReadyForCarriers extends And(
    new EmployingCarriers,
    new Or(new EmployingTwoBase, new BasesAtLeast(3)),
    new Or(new ScoutCleared, new FrameAtLeast(GameTime(4, 45)())))

  class ReadyForArbiters extends Or(
    new EmployingArbiters,
    new And(new UnitsAtLeast(12, Protoss.Carrier), new GasPumpsAtLeast(5), new MiningBasesAtLeast(4)))

  class TechVsBio extends If(
    new Or(
      new EmployingCarriers,
      new UnitsAtLeast(1, Protoss.RoboticsFacility)),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.RoboticsSupportBay)),
    new Parallel(
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives),
        Get(Protoss.PsionicStorm),
        Get(2, Protoss.Forge),
        Get(Protoss.GroundArmor),
        Get(Protoss.GroundArmor),
        Get(Protoss.ZealotSpeed))))

  class TechToCarriers extends Parallel(
    new If(
      new BasesAtLeast(3),
      new Parallel(
        new Build(
          Get(Protoss.Stargate),
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.FleetBeacon),
          Get(Protoss.ZealotSpeed)))),
      new Build(
        Get(Protoss.Stargate),
        Get(Protoss.FleetBeacon)),
    new If(
      new UnitsAtLeast(1, Protoss.FleetBeacon),
      new If(
        new GasPumpsAtLeast(3),
        new Build(
          Get(3, Protoss.Stargate),
          Get(3, Protoss.Carrier)),
        new Build(
          Get(2, Protoss.Stargate),
          Get(2, Protoss.Carrier)))))

  class TechToArbiters extends Build(
    Get(Protoss.CitadelOfAdun),
    Get(Protoss.TemplarArchives),
    Get(Protoss.Stargate),
    Get(Protoss.ArbiterTribunal))

  class AddPrimaryTech extends Parallel(
    new If(
      new EnemyStrategy(With.fingerprints.bio),
      new TechVsBio,
      new Parallel(
        new If(new ReadyForCarriers, new TechToCarriers),
        new If(new ReadyForArbiters, new TechToArbiters))))

  def gatewaysMinimum: Int = Seq(
    AllMiningBases().size - (if (new SafeAtHome().isComplete) 1 else 0),
    (if (With.fingerprints.oneRaxFE.matches)          1 else 0),
    (if (With.fingerprints.siegeExpand.matches)       2 else 0),
    (if (With.fingerprints.twoFac.matches)            3 else 0),
    (if (With.fingerprints.twoFacVultures.matches)    4 else 0),
    (if (With.fingerprints.threeFac.matches)          4 else 0),
    (if (With.fingerprints.threeFacVultures.matches)  5 else 0),
    Math.ceil(1.5 * With.units.countEnemy(Terran.Factory)).toInt
  ).max
  def gatewaysMaximum: Int = 4 * AllMiningBases().size

  class HaveMinimumGateways extends Check(() => With.units.countOurs(Protoss.Gateway) >= Math.min(gatewaysMinimum, gatewaysMaximum))
  class BuildMinimumGateways extends Plan { override def onUpdate() { With.scheduler.request(this, Seq(Get(Math.min(gatewaysMinimum, gatewaysMaximum), Protoss.Gateway))) }}
  class BuildMaximumGateways extends Plan { override def onUpdate() { With.scheduler.request(this, Seq(Get(gatewaysMaximum, Protoss.Gateway))) }}

  override val buildPlans = Vector(

    // Gas management
    new If(
      new GasCapsUntouched,
      new If(
        new And(
          new GasForUpgrade(Protoss.DragoonRange),
          new UnitsAtLeast(1, Protoss.Dragoon),
          new BasesAtMost(1)),
        new If(
          new Employing(PvT21Nexus, PvT28Nexus),
          new CapGasWorkersAt(1),
          new If(
            new Employing(PvT1015Expand, PvT2GateRangeExpand),
            new CapGasWorkersAt(2))))),

    new If(
      new And(
        new EnemiesAtMost(7, Terran.Factory),
        new Or(new UnitsAtLeast(2, Protoss.Observer, complete = true), new Not(new EnemyHasShownWraithCloak), new Not(new EnemyHasShown(Terran.SpiderMine)))),
      new MonitorBases(Protoss.Observer)),

    new EjectScout,
    new BasicOpening,
    new RequireMiningBases(2),

    // Build our main techs
    new If(new WantObservers, new GetObserverTech),
    new If(new Or(new HaveMinimumGateways, new UnitsAtLeast(1, Protoss.Stargate)), new AddPrimaryTech),

    // Expand
    new If(new ReadyForThirdBase,   new RequireBases(3)),
    new If(new ReadyForFourthBase,  new Parallel(new RequireMiningBases(3), new RequireBases(4))),
    new If(new ReadyForFifthBase,   new Parallel(new RequireMiningBases(4), new RequireBases(5))),
    new If(new Or(new EmployingTwoBase, new BasesAtLeast(3), new UnitsAtLeast(5, Protoss.Gateway)), new BuildGasPumps),

    // Protect expansions
    new If(
      new EnemyHasShown(Terran.Vulture),
      new Parallel(
        new If(new UnitsAtLeast(3, Protoss.Gateway), new BuildCannonsAtExpansions(1)),
        new If(new BasesAtLeast(3), new BuildCannonsAtNatural(1)))),

    new HighPriorityTech,
    new PvTIdeas.TrainArmy,
    new BuildMinimumGateways,
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new RequireMiningBases(3),
        new LowPriorityUpgrades,
        new GetObserverTech),
      new BuildMaximumGateways),

    new RequireMiningBases(6),
    new Build(Get(24, Protoss.Gateway))
  )
}


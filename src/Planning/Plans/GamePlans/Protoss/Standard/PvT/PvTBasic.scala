package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.EjectScout
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
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{EnemyHasShownWraithCloak, _}
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyIsRandom, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWorkers}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._

class PvTBasic extends GameplanTemplate {
  override val activationCriteria = new Employing(
    PvT13Nexus,
    PvT21Nexus,
    PvT28Nexus,
    PvT32Nexus,
    PvT2GateRangeExpand,
    PvTDTExpand,
    PvT1GateRobo,
    PvT2BaseCarrier,
    PvT2BaseArbiter,
    PvT3BaseCarrier,
    PvT3BaseArbiter)

  override val meldArchonsAt: Int = -1

  override def scoutPlan: Plan = new Parallel(
    new If(new EnemyIsRandom,                   new ScoutOn(Protoss.Pylon)), // Continue scouting from a PvR opening
    new If(new Employing(PvT13Nexus),           new ScoutOn(Protoss.Nexus, quantity = 2)),
    new If(new Employing(PvT21Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT28Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT32Nexus),           new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT2GateRangeExpand),  new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT1GateRobo),         new ScoutOn(Protoss.CyberneticsCore)),
    new If(new Employing(PvT1015DT),            new If(new UpgradeStarted(Protoss.DragoonRange), new Scout)),
    new If(new Employing(PvTDTExpand),          new ScoutOn(Protoss.CyberneticsCore)))

  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val attackPlan = new Parallel(
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

  override def emergencyPlans: Seq[Plan] = Vector(new PvTIdeas.ReactToBBS, new PvTIdeas.ReactToWorkerRush)

  override def buildOrderPlan: Plan = new Parallel(
    new If(new Employing(PvT13Nexus),           new BuildOrder(ProtossBuilds.PvT13Nexus_GateCoreGateZ: _*)),
    new If(new Employing(PvT21Nexus),           new BuildOrder(ProtossBuilds.PvT21Nexus: _*)),
    new If(new Employing(PvT28Nexus),           new BuildOrder(ProtossBuilds.PvT28Nexus: _*)),
    new If(new Employing(PvT32Nexus),           new BuildOrder(ProtossBuilds.PvT32Nexus: _*)),
    new If(new Employing(PvT2GateRangeExpand),  new BuildOrder(ProtossBuilds.PvT2GateRangeExpand: _*)),
    new If(new Employing(PvT1015DT),            new BuildOrder(ProtossBuilds.PvT1015GateGoonDT: _*)),
    new If(new Employing(PvT1GateRobo),         new BuildOrder(ProtossBuilds.PvT1GateReaver: _*)),
    // If going DT, avoid showing the tech
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

  class EmployingThreeBase  extends Employing(PvT3BaseCarrier, PvT3BaseArbiter)
  class EmployingTwoBase    extends Not(new EmployingThreeBase)
  class CarriersCountered   extends Check(() => With.units.countEnemy(Terran.Goliath) > Math.max(10, With.units.countOurs(Protoss.Interceptor) / 1.8))
  class EmployingCarriers   extends Employing(PvT2BaseCarrier, PvT3BaseCarrier)
  class EmployingArbiters   extends Employing(PvT2BaseArbiter, PvT3BaseArbiter)
  class EnemyTwoPlusRax     extends EnemyStrategy(With.fingerprints.bbs, With.fingerprints.twoRax1113)
  class EnemyTwoPlusFac     extends EnemyStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac, With.fingerprints.twoFacVultures, With.fingerprints.threeFacVultures)

  class UpgradeCarriers extends If(
    new And(new EmployingCarriers, new UnitsAtLeast(2, Protoss.Stargate), new UnitsAtLeast(1, Protoss.FleetBeacon)),
    new Parallel(
      new If(
        new UnitsAtLeast(2, Protoss.CyberneticsCore),
        new FlipIf(new EnemyStrategy(With.fingerprints.bio), new UpgradeContinuously(Protoss.AirDamage), new UpgradeContinuously(Protoss.AirArmor)),
        new If(
          new EnemyStrategy(With.fingerprints.bio),
          new Parallel(new UpgradeContinuously(Protoss.AirArmor), new If(new UpgradeComplete(Protoss.AirArmor, 3), new UpgradeContinuously(Protoss.AirDamage))),
          new Parallel(new UpgradeContinuously(Protoss.AirDamage), new If(new UpgradeComplete(Protoss.AirDamage, 3), new UpgradeContinuously(Protoss.AirArmor)))))))

  class HighPriorityTech extends Parallel(
    new If(new EnemyHasShownWraithCloak, new GoObs),
    new If(
      new And(new EmployingCarriers, new UnitsAtLeast(1, Protoss.Stargate)),
      new BuildOrder(Get(2, Protoss.Stargate), Get(Protoss.FleetBeacon), Get(2, Protoss.Carrier), Get(Protoss.AirDamage))),
    new If(
      new And(new EmployingArbiters, new UnitsAtLeast(1, Protoss.Stargate), new UnitsAtLeast(1, Protoss.CitadelOfAdun)),
      new BuildOrder(Get(Protoss.CitadelOfAdun), Get(Protoss.Stargate), Get(Protoss.TemplarArchives), Get(Protoss.ArbiterTribunal), Get(Protoss.ArbiterEnergy), Get(Protoss.Arbiter))),
    new UpgradeContinuously(Protoss.CarrierCapacity),
    new If(
      new Or(
        new UnitsAtLeast(1, Protoss.HighTemplar),
        new GasAtLeast(1200),
        new And(
          new EmployingCarriers,
          new UnitsAtMost(0, Protoss.RoboticsSupportBay),
          new GasPumpsAtLeast(3),
          new Or(
            new CarriersCountered,
            new EnemiesAtLeast(8, Terran.Goliath),
            new GasPumpsAtLeast(5)))),
      new GoStorm),

    new If(new UnitsAtLeast(1, Protoss.ArbiterTribunal),  new Build(Get(Protoss.ArbiterEnergy))),
    new If(new UpgradeComplete(Protoss.ArbiterEnergy),    new Build(Get(Protoss.Stasis))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),           new UpgradeContinuously(Protoss.ZealotSpeed)),

    new UpgradeCarriers,
    new If(new And(new UnitsAtLeast(1, Protoss.Shuttle), new GasPumpsAtLeast(2)), new UpgradeContinuously(Protoss.ShuttleSpeed)),
    new If(
      new Or(new MineralOnlyBase, new And(new MiningBasesAtLeast(4), new EmployingCarriers), new And(new MiningBasesAtLeast(5))),
      new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))))

  class LowPriorityTech extends Parallel(
    new If(new And(new EmployingArbiters, new GasPumpsAtLeast(4), new UnitsAtLeast(1, Protoss.ArbiterTribunal)),  new Build(Get(2, Protoss.Stargate), Get(2, Protoss.Forge))),
    new If(new And(new EmployingCarriers, new GasPumpsAtLeast(4), new UnitsAtLeast(1, Protoss.FleetBeacon)),      new Build(Get(2, Protoss.CyberneticsCore), Get(Protoss.Forge))),
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
            new If(new UpgradeComplete(Protoss.GroundDamage, 3), new UpgradeContinuously(Protoss.GroundArmor)))))),

    new If( new And(new EnemiesAtLeast(3, Terran.SpiderMine), new GasPumpsAtLeast(3)), new UpgradeContinuously(Protoss.ObserverSpeed)))

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

  class ConsiderTakingFastThirdBase extends If(
    new And(
      new Latch(new BasesAtLeast(2)),
      new Or(
        new EmployingThreeBase,
        new UnitsAtLeast(4, Protoss.Carrier),
        new UnitsAtLeast(1, Protoss.Arbiter)),
      new SafeToMoveOut,
      new Or(
        // We can always match base count
        new EnemyBasesAtLeast(3),
        new EnemiesAtLeast(3, Terran.CommandCenter),
        // Against gasless openings, only need 1 Gate Obs to take third base
        new And(
          new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE),
          new Or(new UnitsAtLeast(1, Protoss.RoboticsFacility), new UnitsAtLeast(1, Protoss.TemplarArchives))),
        // Against one-fac expansions, we just need Obs + 3 Gate to take third base
        new And(
          new EnemyStrategy(With.fingerprints.siegeExpand, With.fingerprints.oneFac),
          new Or(
            new EnemyNaturalConfirmed,
            new EnemyHasShown(Terran.Comsat),
            new EnemyHasShown(Terran.SpellScannerSweep),
            new EnemiesAtLeast(1, Terran.MissileTurret)),
          new UnitsAtLeast(3, Protoss.Gateway)))),
    new RequireMiningBases(3))

  class ConsiderTakingFastFourthBase extends If(
    new And(
      new Latch(new BasesAtLeast(3)),
      new SafeToMoveOut,
      new Or(
        // We can always match base count
        new EnemyBasesAtLeast(4),
        new EnemiesAtLeast(4, Terran.CommandCenter),
        // Vs Armory + 3rd CC can take 4 bases
        new And(
          new EnemiesAtLeast(3, Terran.CommandCenter),
          new EnemyStrategy(With.fingerprints.oneArmoryUpgrades)),
        // Vs Double Armory (or Armory + Starport) can take 4-5 base
        new EnemyStrategy(With.fingerprints.twoArmoryUpgrades))),
    new Parallel(
      new RequireMiningBases(3),
      new RequireBases(4)))

  class GoObs extends BuildOrder(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer))
  class GoReaver extends BuildOrder(Get(Protoss.RoboticsFacility), Get(Protoss.Shuttle), Get(Protoss.RoboticsSupportBay), Get(2, Protoss.Reaver))
  class GoDT extends BuildOrder(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives), Get(2, Protoss.DarkTemplar))
  class GoStorm extends BuildOrder(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives), Get(Protoss.PsionicStorm), Get(4, Protoss.HighTemplar))

  override val buildPlans = Vector(

    ////////////////
    // Logistics //
    //////////////

    new EjectScout,

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

    // Scout with Observer
    new If(
      new And(
        new EnemiesAtMost(7, Terran.Factory),
        new Or(new UnitsAtLeast(2, Protoss.Observer, complete = true), new Not(new EnemyHasShownWraithCloak), new Not(new EnemyHasShown(Terran.SpiderMine)))),
      new MonitorBases(Protoss.Observer)),

    // Pylon block
    new If(new And(new MiningBasesAtLeast(3), new UnitsAtLeast(48, UnitMatchWorkers)), new PylonBlock),

    // Protect expansions
    new If(
      new EnemyHasShown(Terran.Vulture),
      new Parallel(
        new If(new UnitsAtLeast(3, Protoss.Gateway), new BuildCannonsAtExpansions(1)),
        new If(new BasesAtLeast(3), new BuildCannonsAtNatural(1)))),

    /////////////////////////
    // Actual build order //
    ///////////////////////

    // TODO: Ensure eventual second base (maybe reuse existing conditions)
    // TODO: Ensure eventual third base (maybe reuse existing conditions)
    new ConsiderTakingFastThirdBase,
    new ConsiderTakingFastFourthBase,

    new BuildOrder(Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.Dragoon), Get(Protoss.DragoonRange)),

    new HighPriorityTech,
    new PvTIdeas.TrainArmy,

    ////////////////////////////
    // Reactions from 1 base //
    //////////////////////////

    // TODO: Against Strong FD (no fingerprint yet) we need a second Gateway

    // One-base two-fac without tech:
    // One-base two-fac with robo:
    //  Obs -> 2 Gate
    // One-base two-fac with citadel+
    //  DT -> 2 Gate
    new If(
      new EnemyTwoPlusFac,
      new If(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new Parallel(new GoDT, new Build(Get(2, Protoss.Gateway))),
        new Parallel(new GoObs, new Build(Get(2, Protoss.Gateway))))),

    // One-base bio without tech
    //  2 Gate -> Reaver
    // One-base bio with robo
    //  Reaver -> 2 gate
    // One-base bio with citadel+
    //  DT -> 3 Gate
    new If(
      new EnemyTwoPlusRax,
      new If(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new Parallel(new GoDT, new Build(Get(3, Protoss.Gateway))),
        new If(
          new UnitsAtLeast(1, Protoss.RoboticsFacility),
          new Parallel(new GoReaver, new Build(Get(2, Protoss.Gateway))),
          new Parallel(new Build(Get(2, Protoss.Gateway)), new GoReaver)))),

    // Take natural!
    new RequireMiningBases(2),

    // Gas pump timing
    new PumpRatio(Protoss.Assimilator, 1, 2, Seq(Friendly(Protoss.Gateway, 0.2), Friendly(Protoss.RoboticsSupportBay, 0.4), Friendly(Protoss.TemplarArchives, 0.2), Friendly(Protoss.Stargate, 2.0))),
    new If(new EmployingTwoBase), new BuildGasPumps,
    new If(new MiningBasesAtLeast(3), new BuildGasPumps),
    new If(new UnitsAtLeast(45, UnitMatchWorkers), new BuildGasPumps),

    ////////////////////////////
    // Reactions from 2 base //
    //////////////////////////

    // One-base two-fac without tech
    //  2 Gate -> Obs -> 5 Gate
    // One-base two-fac with robo
    //  Obs -> 5 Gate
    // One-base two-fac with citadel+
    //  DT -> 3 Gate -> Obs -> 5 Gate
    new If(
      new EnemyTwoPlusFac,
      new If(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new Parallel(new GoDT, new Build(Get(3, Protoss.Gateway)), new GoObs, new Build(Get(5, Protoss.Gateway))),
        new If(
          new UnitsAtLeast(1, Protoss.RoboticsFacility),
          new Parallel(new GoObs, new Build(Get(5, Protoss.Gateway))),
          new Parallel(new Build(Get(2, Protoss.Gateway)), new GoObs, new Build(Get(5, Protoss.Gateway)))))),

    // TODO: Against 3Fac: Maybe go up to 6 Gateways? See if it's necessary first

    // Two-base bio without tech
    //  Carrier build: Reaver -> 5 Gate
    //  Arbiter build: 3 Gate -> Storm -> 5 Gate
    // Two-base bio with robo
    //  Reaver -> 5 Gate
    // Two-base bio with citadel+
    //  Storm -> 5 Gate
    new If(
      new EnemyStrategy(With.fingerprints.bio),
      new If(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new Parallel(new GoStorm, new Build(Get(5, Protoss.Gateway))),
        new If(
          new UnitsAtLeast(1, Protoss.RoboticsFacility),
          new Parallel(new GoReaver, new Build(Get(5, Protoss.Gateway))),
          new If(
            new EmployingCarriers,
            new Parallel(new GoReaver, new Build(Get(5, Protoss.Gateway))),
            new Parallel(new GoStorm, new Build(Get(5, Protoss.Gateway))))))),

    // Two-base tech: Go get your tech
    // Three-base tech: Get Observer + Gateways and react
    new If(
      new EmployingThreeBase,
      new Parallel(new GoObs, new Build(Get(3, Protoss.Gateway))),
      new If(
        // Hide our tech
        new Or(new ScoutCleared, new FrameAtLeast(GameTime(4, 45)())),
        new Parallel(
          new If(
            new EmployingCarriers,
            new BuildOrder(Get(2, Protoss.Stargate), Get(Protoss.FleetBeacon), Get(2, Protoss.Carrier), Get(Protoss.CarrierCapacity), Get(4, Protoss.Carrier))),
          new If(
            new EmployingArbiters,
            new BuildOrder(Get(Protoss.CitadelOfAdun), Get(2, Protoss.Gateway), Get(Protoss.Stargate), Get(Protoss.TemplarArchives), Get(Protoss.ArbiterTribunal), Get(Protoss.ArbiterEnergy), Get(Protoss.Arbiter), Get(Protoss.Stasis)))))),

    // Get enough Gateways (and other production facilities) to survive
    new PumpRatio(Protoss.Gateway, 3, 7, Seq(Enemy(Terran.Barracks, 1.0), Enemy(Terran.Factory, 1.5), Friendly(Protoss.Stargate, -2.0), Friendly(Protoss.RoboticsSupportBay, -1.0))),

    new RequireMiningBases(3),
    new LowPriorityTech,
    new RequireMiningBases(5),
    new Build(Get(22, Protoss.Gateway))
  )
}


package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.EjectScout
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{Or, Parallel, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTIdeas.PvTAttack
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Placement.{BuildCannonsAtExpansions, BuildCannonsAtNatural, ProposePlacement}
import Planning.Plans.Scouting.{MonitorBases, ScoutCleared, ScoutOn, ScoutWithWorkers}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Economy.{GasAtLeast, GasAtMost}
import Planning.Predicates.Milestones.{EnemyHasShownWraithCloak, _}
import Planning.Predicates.Reactive._
import Planning.Predicates.Strategy.{Employing, EnemyIsRandom, EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWorkers}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss._

class PvTBasic extends GameplanTemplate {

  override def archonPlan: Plan = new If(
    new EnemyStrategy(With.fingerprints.bio),
    new MeldArchons(49) { override def maximumTemplar: Int = 8 },
    new MeldArchons(25))

  override def placementPlan: Plan = new If(
    new And(
      new FrameAtMost(GameTime(5, 0)()),
      new Employing(PvT32Nexus),
      new Not(new EnemyRecentStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.workerRush))),
    new ProposePlacement(
      new Blueprint(Protoss.Pylon,           preferZone = Some(With.geography.ourNatural.zone)),
      new Blueprint(Protoss.Gateway,         preferZone = Some(With.geography.ourNatural.zone)),
      new Blueprint(Protoss.Pylon,           requireZone = Some(With.geography.ourMain.zone)),
      new Blueprint(Protoss.CyberneticsCore, requireZone = Some(With.geography.ourMain.zone))))

  override def initialScoutPlan: Plan = new Parallel(
    new If(new EnemyIsRandom,                   new ScoutOn(Protoss.Pylon)), // Continue scouting from a PvR opening
    new If(new Employing(PvT13Nexus),           new ScoutOn(Protoss.Nexus, quantity = 2)),
    new If(new Employing(PvT21Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT28Nexus),           new ScoutOn(Protoss.Gateway)),
    new If(new Employing(PvT32Nexus),           new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT2GateRangeExpand),  new ScoutOn(Protoss.Pylon)),
    new If(new Employing(PvT1GateReaver),       new ScoutOn(Protoss.CyberneticsCore)),
    new If(new Employing(PvT1015DT),            new If(new UpgradeStarted(Protoss.DragoonRange), new ScoutWithWorkers)),
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
        new Not(new Employing(PvTDTExpand, PvT1GateReaver)),
        new Latch(new UnitsAtLeast(1, UnitMatchOr(Protoss.DarkTemplar, Protoss.Reaver), complete = true)),
        new UpgradeStarted(Protoss.DragoonRange)),
      new PvTIdeas.AttackSafely))

  override def emergencyPlans: Seq[Plan] = Vector(new PvTIdeas.ReactToBBS, new PvTIdeas.ReactToWorkerRush)

  override def buildOrderPlan: Plan = new Parallel(
    new ConsiderTakingFastSecondBase,
    new If(new Employing(PvT13Nexus),           new BuildOrder(ProtossBuilds.PvT13Nexus_GateCoreGateZ: _*)),
    new If(new Employing(PvT21Nexus),           new BuildOrder(ProtossBuilds.PvT21Nexus: _*)),
    new If(new Employing(PvT28Nexus),           new BuildOrder(ProtossBuilds.PvT28Nexus: _*)),
    new If(new Employing(PvT32Nexus),           new BuildOrder(ProtossBuilds.PvT32Nexus: _*)),
    new If(new Employing(PvT2GateRangeExpand),  new BuildOrder(ProtossBuilds.PvT2GateRangeExpand: _*)),
    new If(new Employing(PvT1015DT),            new BuildOrder(ProtossBuilds.PvT1015GateGoonDT: _*)),
    new If(new Employing(PvT1GateReaver),       new BuildOrder(ProtossBuilds.PvT1GateReaver: _*)),
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

  class EmployingThreeBase  extends Employing(PvT3BaseCarrier, PvT3BaseArbiter, PvT3BaseGateway)
  class EmployingTwoBase    extends Not(new EmployingThreeBase)
  class CarriersCountered   extends Check(() =>
      1.8 * With.units.countEnemy(Terran.Goliath)
    + 1.0 * With.units.countEnemy(Terran.Marine)
    + 2.0 * With.units.countEnemy(Terran.Medic) > Math.max(10, With.units.countOurs(Protoss.Interceptor)))
  //class EmployingGateway    extends Or(new Employing(PvT2BaseGateway, PvT3BaseGateway), new And(new EnemyStrategy(With.fingerprints.bio), new UnitsAtMost(0, Protoss.FleetBeacon), new UnitsAtMost(0, Protoss.ArbiterTribunal)))
  class EmployingGateway    extends Employing(PvT2BaseGateway, PvT3BaseGateway)
  class EmployingCarriers   extends And(new Employing(PvT2BaseCarrier, PvT3BaseCarrier), new Not(new EmployingGateway))
  class EmployingArbiters   extends Or(
    new And(new Not(new EmployingGateway),  new Employing(PvT2BaseArbiter, PvT3BaseArbiter)),
    new And(new EmployingGateway,           new BasesAtLeast(4), new GasPumpsAtLeast(3), new UpgradeStarted(Protoss.GroundDamage), new UpgradeStarted(Protoss.GroundArmor)))
  class EmployingReavers    extends Or(new Employing(PvT2BaseReaver), new UnitsAtLeast(1, Protoss.RoboticsSupportBay))
  class EmployingDTRush     extends Or(new UnitsAtLeast(1, Protoss.TemplarArchives), new Employing(PvTStove, PvTDTExpand, PvT2BaseArbiter))
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

  // Get upgrades with Arbiter builds, vs. Bio, or when maxed on air upgrades
  // Double-spin, or prioritize armor vs. bio
  class GatewayUpgrades extends If(
    new Or(
      new EmployingArbiters,
      new EmployingGateway,
      new And(
        new UpgradeComplete(Protoss.AirDamage, 2, Protoss.AirDamage.upgradeFrames(2)),
        new UpgradeComplete(Protoss.AirArmor, 2, Protoss.AirArmor.upgradeFrames(2)))),
    new Parallel(
      new If(
        new And(new UpgradeStarted(Protoss.GroundDamage), new UpgradeStarted(Protoss.GroundArmor)),
        new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives))),
      new If(new TechComplete(Protoss.PsionicStorm), new UpgradeContinuously(Protoss.HighTemplarEnergy)),
      new If(
        new UnitsAtLeast(2, Protoss.Forge),
        new FlipIf(
          new EnemyStrategy(With.fingerprints.bio),
          new UpgradeContinuously(Protoss.GroundDamage),
          new UpgradeContinuously(Protoss.GroundArmor)),
        new If(
          new EnemyStrategy(With.fingerprints.bio),
          new If(
            new UpgradeComplete(Protoss.GroundArmor, 3),
            new UpgradeContinuously(Protoss.GroundDamage),
            new UpgradeContinuously(Protoss.GroundArmor)),
          new If(
            new UpgradeComplete(Protoss.GroundDamage, 3),
            new UpgradeContinuously(Protoss.GroundArmor),
            new UpgradeContinuously(Protoss.GroundDamage)))),
      new If(new UnitsAtLeast(3, Protoss.Forge), new UpgradeContinuously(Protoss.Shields))))

  class HighPriorityTech extends Parallel(
    new If(new EnemyHasShownWraithCloak, new GoObs),

    new UpgradeContinuously(Protoss.CarrierCapacity),

    new If(new Or(new UnitsAtLeast(1, Protoss.HighTemplar), new GasAtLeast(800)), new GoStorm),

    new If(new UnitsAtLeast(1, Protoss.ArbiterTribunal),  new Build(Get(Protoss.ArbiterEnergy))),
    new If(new UpgradeComplete(Protoss.ArbiterEnergy),    new Build(Get(Protoss.Stasis))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),           new UpgradeContinuously(Protoss.ZealotSpeed)),

    new UpgradeCarriers,
    new If(
      new And(new UnitsAtLeast(1, Protoss.Shuttle), new UnitsAtLeast(1, Protoss.Reaver), new GasPumpsAtLeast(2)),
      new UpgradeContinuously(Protoss.ShuttleSpeed)),
    new If(
      new Or(new MineralOnlyBase, new And(new MiningBasesAtLeast(4), new EmployingCarriers), new And(new MiningBasesAtLeast(5))),
      new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new GatewayUpgrades)

  class LowPriorityTech extends Parallel(
    new If(new And(new EmployingGateway, new GasPumpsAtLeast(3)), new Build(Get(2, Protoss.Forge))),
    new If(new And(new EmployingGateway, new GasPumpsAtLeast(4)), new Build(Get(3, Protoss.Forge))),
    new If(new And(new EmployingArbiters, new GasPumpsAtLeast(4), new UnitsAtLeast(1, Protoss.ArbiterTribunal)),  new Build(Get(2, Protoss.Stargate), Get(2, Protoss.Forge))),
    new If(new And(new EmployingCarriers, new GasPumpsAtLeast(4), new UnitsAtLeast(1, Protoss.FleetBeacon)),      new Build(Get(2, Protoss.CyberneticsCore), Get(Protoss.Forge))),
    new If(new EmployingCarriers, new UpgradeContinuously(Protoss.Shields, 1)),
    new GatewayUpgrades,
    new If(new And(new EnemiesAtLeast(3, Terran.SpiderMine), new GasPumpsAtLeast(3)), new UpgradeContinuously(Protoss.ObserverSpeed)),
    new If(new UnitsAtLeast(2, Protoss.Reaver), new UpgradeContinuously(Protoss.ScarabDamage)))

  class ConsiderTakingFastSecondBase extends If(
    new And(
      new UnitsAtLeast(1, Protoss.CyberneticsCore),
      new Employing(PvT21Nexus, PvT28Nexus, PvT32Nexus),
      new Not(new EnemyStrategy(With.fingerprints.bunkerRush, With.fingerprints.bbs, With.fingerprints.twoRax1113)),
      new Or(
        new EnemyNaturalConfirmed,
        new EnemiesAtLeast(1, Terran.Bunker))),
    new RequireBases(2))

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
        // DT openings can survive anything but mass Vulture
        new And(
          new UnitsAtLeast(1, Protoss.TemplarArchives),
          new Or(
            new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE, With.fingerprints.siegeExpand),
            new And(
              new EnemiesAtLeast(1, Terran.MissileTurret),
              new Not(new EnemyTwoPlusFac)))),
        // Against gasless openings, only need 1 Gate Obs or DT to take third base
        new And(
          new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE),
          new UnitsAtLeast(1, Protoss.RoboticsFacility)),
        // Against one-fac expansions, we just need Obs + 3 Gate to take third base
        new And(
          new EnemyStrategy(With.fingerprints.siegeExpand, With.fingerprints.oneFac),
          new Or(
            new EnemyNaturalConfirmed,
            new EnemyHasShown(Terran.Comsat),
            new EnemyHasShown(Terran.SpellScannerSweep),
            new EnemiesAtLeast(1, Terran.MissileTurret)),
          new UnitsAtLeast(3, Protoss.Gateway)),
        // Bunker is a good hint of safety but not absolute proof; consider it but don't get fooled too often
        new And(
          new EnemyStrategy(With.fingerprints.oneFac),
          new EnemiesAtLeast(1, Terran.Bunker),
          new Not(new EnemyStrategy(With.fingerprints.bunkerRush)),
          new Not(new EnemyRecentStrategy(With.fingerprints.twoFac, With.fingerprints.threeFac, With.fingerprints.twoFacVultures, With.fingerprints.threeFacVultures))))),
    new Parallel(new WriteStatus("Instant3rd"), new RequireMiningBases(3)))

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
        new EnemyStrategy(With.fingerprints.twoArmoryUpgrades)),
        new And(
          new EnemyStrategy(With.fingerprints.bio),
          new UnitsAtLeast(1, Protoss.Arbiter, complete = true))),
    new Parallel(
      new WriteStatus("Instant4th"),
      new RequireMiningBases(3),
      new RequireBases(4)))

  class GoObs     extends Parallel(new WriteStatus("GoObs"),      new BuildOrder(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.Observer)))
  class GoReaver  extends Parallel(new WriteStatus("GoReaver"),   new BuildOrder(Get(Protoss.RoboticsFacility), Get(Protoss.Shuttle), Get(Protoss.RoboticsSupportBay), Get(Protoss.Reaver)))
  class GoDT      extends Parallel(new WriteStatus("GoDT"),       new BuildOrder(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives), Get(2, Protoss.DarkTemplar)))
  class GoStorm   extends Parallel(new WriteStatus("GoStorm"),    new BuildOrder(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives), Get(Protoss.PsionicStorm), Get(2, Protoss.HighTemplar)))
  class GoCarrier extends Parallel(new WriteStatus("GoCarrier"),  new BuildOrder(Get(Protoss.Stargate), Get(2, Protoss.Gateway), Get(2, Protoss.Stargate), Get(Protoss.FleetBeacon), Get(2, Protoss.Carrier), Get(4, Protoss.Gateway), Get(Protoss.CarrierCapacity), Get(Protoss.AirDamage)))
  class GoArbiter extends Parallel(new WriteStatus("GoArbiter"),  new BuildOrder(Get(Protoss.CitadelOfAdun), Get(2, Protoss.Gateway), Get(Protoss.Stargate), Get(Protoss.TemplarArchives), Get(Protoss.ArbiterTribunal), Get(Protoss.ArbiterEnergy), Get(Protoss.Arbiter), Get(6, Protoss.Gateway)))
  class GoGateway extends Parallel(new WriteStatus("GoGateway"),  new BuildOrder(Get(3, Protoss.Gateway), Get(Protoss.CitadelOfAdun), Get(2, Protoss.Forge), Get(5, Protoss.Gateway), Get(Protoss.GroundDamage), Get(Protoss.GroundArmor), Get(Protoss.ZealotSpeed), Get(Protoss.TemplarArchives), Get(7, Protoss.Gateway)))

  class PumpReactiveGateways(min: Int, max: Int) extends PumpRatio(Protoss.Gateway, min, max, Seq(Enemy(Terran.Barracks, 1.0), Enemy(Terran.Factory, 1.5), Friendly(Protoss.Stargate, -2.0), Friendly(Protoss.RoboticsSupportBay, -1.0)))

  override val buildPlans = Vector(

    ////////////////
    // Logistics //
    //////////////

    new EjectScout,

    // Gas management
    new If(
      // Allow emergency reactions to control gas
      new GasCapsUntouched,
      new Parallel(
        new If(
          // On one base, we usually just need the gas for 1-2 gate Goon production
          new BasesAtMost(1),
          new If(
            new And(new GasForUpgrade(Protoss.DragoonRange), new UnitsAtLeast(1, Protoss.Dragoon)),
            new If(
              new Employing(PvT21Nexus, PvT28Nexus, PvT32Nexus),
              new CapGasWorkersAt(1),
              new If(
                new Employing(PvT1015Expand, PvT2GateRangeExpand),
                new CapGasWorkersAt(2),
                new Parallel(
                  new If(
                    new And(new Employing(PvTDTExpand), new UnitsAtMost(0, Protoss.DarkTemplar), new GasAtMost(300)),
                    new FloorGasWorkersAt(3))))))),
          new If(
            // On two base, we only want gas if we're fast teching
            new BasesAtMost(2),
            new If(
              new And(
                new EnemyTwoPlusFac,
                new UnitsAtMost(4, Protoss.Gateway)),
              new CapGasAt(250)),
            new If(
              new UnitsAtMost(45, UnitMatchWorkers),
              new CapGasAt(500))))),

    // Scout with Observer
    new If(
      new And(
        new EnemiesAtMost(7, Terran.Factory),
        new Or(
          new UnitsAtLeast(2, Protoss.Observer, complete = true),
          new And(
            new Not(new EnemyHasShownWraithCloak),
            new Not(new PvTIdeas.EnemyHasMines)))),
      new MonitorBases(Protoss.Observer)),

    // Pylon block
    new If(new And(new BasesAtLeast(3), new MiningBasesAtLeast(2), new UnitsAtLeast(45, UnitMatchWorkers)), new PylonBlock),

    /////////////////////////
    // Actual build order //
    ///////////////////////

    new ConsiderTakingFastThirdBase,
    new ConsiderTakingFastFourthBase,

    new BuildOrder(Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.Dragoon), Get(Protoss.DragoonRange)),
    new If(new And(new EmployingReavers,  new UnitsAtLeast(1, Protoss.RoboticsFacility)), new GoReaver),
    new If(
      new And(new SafeAtHome, new UnitsAtLeast(5, Protoss.Gateway)),
      new Parallel(
        new If(new And(new EmployingCarriers, new UnitsAtLeast(1, Protoss.Stargate)),       new GoCarrier),
        new If(new And(new EmployingGateway,  new UnitsAtLeast(1, Protoss.CitadelOfAdun)),  new GoStorm),
        new If(new And(new EmployingArbiters, new UnitsAtLeast(1, Protoss.CitadelOfAdun)),  new GoArbiter))),

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
        new EmployingDTRush,
        new Parallel(new GoDT, new Build(Get(2, Protoss.Gateway))),
        new If(
          new EmployingReavers,
          new Parallel(new GoReaver, new Build(Get(2, Protoss.Gateway))),
          new Parallel(new GoObs, new Build(Get(2, Protoss.Gateway)))))),

    // One-base bio without tech
    //  2 Gate -> Reaver
    // One-base bio with robo
    //  Reaver -> 2 gate
    // One-base bio with citadel+
    //  DT -> 3 Gate
    new If(
      new EnemyTwoPlusRax,
      new If(
        new EmployingDTRush,
        new GoDT, // DT stream will keep us alive until Vessels
        new If(
          new UnitsAtLeast(1, Protoss.RoboticsFacility),
          new Parallel(new GoReaver, new Build(Get(2, Protoss.Gateway))),
          new Parallel(new Build(Get(2, Protoss.Gateway)), new GoReaver)))),

    // Take natural!
    new RequireMiningBases(2),

    // Gas pump timing
    new PumpRatio(Protoss.Assimilator, 1, 2, Seq(Friendly(Protoss.Gateway, 0.2), Friendly(Protoss.RoboticsSupportBay, 0.4), Friendly(Protoss.TemplarArchives, 0.2), Friendly(Protoss.Stargate, 2.0))),
    new If(new EmployingTwoBase, new BuildGasPumps),
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
        new And(new EmployingDTRush, new UnitsAtLeast(1, Protoss.CitadelOfAdun)),
        new Parallel(new GoDT, new Build(Get(3, Protoss.Gateway)), new GoObs, new Build(Get(5, Protoss.Gateway))),
        new If(
          new EmployingReavers,
            new Parallel(new GoReaver, new Build(Get(3, Protoss.Gateway), Get(2, Protoss.Assimilator))),
            new Parallel(
              new If(new UnitsAtMost(0, Protoss.RoboticsFacility), new Build(Get(2, Protoss.Gateway))),
              new GoObs,
              new Build(Get(4, Protoss.Gateway), Get(2, Protoss.Assimilator), Get(5, Protoss.Gateway)))))),

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
        new EmployingDTRush,
        new GoDT, // DT stream will keep us alive until Vessels
        new If(
          new Or(new EmployingReavers, new EmployingCarriers, new UnitsAtLeast(1, Protoss.RoboticsFacility)),
          new Parallel(new GoReaver, new Build(Get(5, Protoss.Gateway))),
          new Parallel(new GoStorm, new Build(Get(5, Protoss.Gateway)))))),

    // Two-base tech: Go get your tech
    // Three-base tech: If fast-thirding, take it now; otherwise get Observer + Gateways and react
    new If(new EmployingReavers, new GoReaver),
    new If(
      new EmployingThreeBase,
      new If(
        new Employing(PvT3rdObs),
        new Parallel(
          new If(new EnemyBasesAtMost(1), new Build(Get(2, Protoss.Gateway))),
          new GoObs,
          new Build(Get(3, Protoss.Gateway)))),
      new If(
        // Hide our tech
        new Or(new ScoutCleared, new FrameAtLeast(GameTime(4, 45)())),
        new Parallel(
          // Without this step 2-Base Reaver into Carrier gets Stargate before even finishing Robo
          new If(new And(new EmployingReavers, new EmployingCarriers), new Build(Get(2, Protoss.Gateway))),
          new If(new EmployingGateway,  new GoGateway),
          new If(new EmployingCarriers, new GoCarrier),
          new If(new EmployingArbiters, new GoArbiter)))),

    // Get enough Gateways (and other production facilities) to survive
    new If(
      new Employing(PvT2BaseCarrier),
      new Build(Get(4, Protoss.Gateway)),
      new If(
        new Employing(PvT2BaseReaver),
        new PumpReactiveGateways(3, 6),
        new If(
          new Employing(PvT2BaseArbiter, PvT2BaseGateway),
          new PumpReactiveGateways(6, 7),
          new PumpReactiveGateways(3, 7)))),

    new RequireMiningBases(3),

    // Get our 3-base tech, but not before 3 bases
    new FlipIf(
      new BasesAtLeast(3),
      // Reactive Gateways
      new Parallel(
        new If(
          new EmployingCarriers,
          new PumpReactiveGateways(5, 8),
          new PumpReactiveGateways(9, 12))),
      // 3-Base tech
      new Parallel(
        new If(new EmployingGateway,  new GoGateway),
        new If(new EmployingCarriers, new GoCarrier),
        new If(new EmployingArbiters, new GoArbiter))),

    new LowPriorityTech,

    // Protect expansions
    new If(
      new EnemyHasShown(Terran.Vulture),
      new Parallel(
        new If(new UnitsAtLeast(3, Protoss.Gateway), new BuildCannonsAtExpansions(1)),
        new If(new BasesAtLeast(3), new BuildCannonsAtNatural(1)))),

    new RequireMiningBases(4),
    new PumpReactiveGateways(12, 16),
    new RequireMiningBases(5),
    new Build(Get(25, Protoss.Gateway)),
    new RequireMiningBases(6),
    new Build(Get(30, Protoss.Gateway))
  )
}


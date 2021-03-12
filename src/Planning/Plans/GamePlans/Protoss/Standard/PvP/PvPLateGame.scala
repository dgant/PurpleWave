package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, PumpWorkers, PylonBlock, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, BuildGasPumpsIfBelow, RequireBases, RequireMiningBases}
import Planning.Plans.Placement.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound._
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.{Employing, EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.{MatchOr, MatchWarriors, MatchWorker}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP3rdBaseFast

class PvPLateGame extends GameplanTemplate {

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToRoboAsDT,
    new PvPIdeas.ReactToArbiters)

  override val attackPlan: Plan = new PvPIdeas.AttackSafely

  override def archonPlan: Plan = new PvPIdeas.MeldArchonsPvP

  override def workerPlan: Plan = new PumpWorkers(maximumTotal = 25)

  val goingTemplar = new Not(new Sticky(new UnitsAtLeast(1, Protoss.RoboticsSupportBay)))

  val buildCannons = new And(
    // It's a good bet to do so; DT are the scariest threat
    new Or(
      new SafeAtHome,
      new EnemyHasShownCloakedThreat),

    // Templar are reasonably likely
    new Or(
      new EnemyRecentStrategy(With.fingerprints.dtRush),
      new ReadyForThirdIfSafe, // We're far enough along
      new And(
        new EnemyBasesAtMost(1),
        new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.fourGateGoon)))),

    // We can't use a Robotics Facility
    // Note that this is deliberately placed after the Templar check so it only sticks at the point we're thinking about DTs
    new Sticky(new UnitsAtMost(0, Protoss.RoboticsFacility)))

  class EnemyPassiveOpening extends Or(
    new And(
      new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
      new EnemyStrategy(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.forgeFe)),
    new EnemyBasesAtLeast(2))

  class AddEarlyTech extends If(
    goingTemplar,
    new Parallel(
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(2, Protoss.Gateway),
        Get(Protoss.TemplarArchives),
        Get(6, Protoss.Gateway)),
      new BuildGasPumpsIfBelow(300),
      new Build(
        Get(Protoss.ZealotSpeed),
        Get(Protoss.Forge),
        Get(Protoss.GroundDamage),
        Get(Protoss.PsionicStorm),
        Get(7, Protoss.Gateway),
        Get(Protoss.HighTemplarEnergy),
        Get(2, Protoss.GroundDamage),
        Get(10, Protoss.Gateway))),

    new Parallel(
      new Build(Get(3, Protoss.Gateway)),
      new If(new UnitsAtLeast(1, Protoss.RoboticsFacility), new BuildGasPumpsIfBelow(150)),
      new FlipIf(
        new And(
          new SafeAtHome,
          new Or(
            new UnitsAtLeast(1, Protoss.RoboticsFacility),
            new EnemyBasesAtLeast(2))),
        new Parallel(
          new Build(
            Get(5, Protoss.Gateway),
            Get(2, Protoss.Assimilator))),
        new Build(
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(Protoss.RoboticsSupportBay),
          Get(Protoss.ShuttleSpeed))),
      new BuildGasPumpsIfBelow(200),
      new Build(Get(6, Protoss.Gateway)),
      new BuildGasPumps))

  class AddLateTech extends Parallel(
    new Build(Get(Protoss.Forge)),
    new If(new GasPumpsAtLeast(4), new Build(Get(2, Protoss.Forge))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new If(new Or(new UpgradeStarted(Protoss.GroundDamage, 3), new UnitsAtLeast(2, Protoss.Forge)), new UpgradeContinuously(Protoss.GroundArmor)),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory)))

  class AddLateGateways extends Parallel(
    new If(new MiningBasesAtLeast(2), new Build(Get(9, Protoss.Gateway))),
    new If(new MiningBasesAtLeast(3), new Build(Get(13, Protoss.Gateway))),
    new If(new MiningBasesAtLeast(4), new Build(Get(16, Protoss.Gateway))),
    new If(new MiningBasesAtLeast(5), new Build(Get(20, Protoss.Gateway))))

  class GetReactiveObservers extends If(
    new And(
      new Or(
        new EnemyHasShown(Protoss.DarkTemplar),
        new EnemyHasShown(Protoss.Arbiter),
        new EnemiesAtLeast(1, Protoss.ArbiterTribunal)),
      new Or(new Not(goingTemplar), new ReadyForThirdIfSafe)),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.ObserverSpeed)))

  class ReadyForThirdIfSafe extends And(
    new Or(
      new Not(new EnemyHasShown(Protoss.DarkTemplar)),
      new UnitsAtLeast(2, Protoss.Observer)),
    new Or(
      new And(
        goingTemplar,
        new UpgradeComplete(Protoss.ZealotSpeed),
        new TechComplete(Protoss.PsionicStorm),
        new UnitsAtLeast(7, Protoss.Gateway)),
      new And(
        new Not(goingTemplar),
        new UnitsAtLeast(5, Protoss.Gateway, complete = true),
        new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
        new UnitsAtLeast(1, Protoss.Reaver, complete = true))))

  class SafeForThird extends And(
    new SafeToMoveOut,
    new Or(
      new EnemyBasesAtLeast(3),
      new Employing(PvP3rdBaseFast),
      new EnemiesAtLeast(5, Protoss.PhotonCannon),
      new And(
        new Not(new EnemyHasShown(Protoss.Shuttle)),
        new Or(
          new EnemyHasShown(Protoss.HighTemplar),
          new EnemyHasShown(Protoss.Reaver)))),
    new Or(
      new Not(new EnemyHasShown(Protoss.DarkTemplar)),
      new UnitsAtLeast(2, Protoss.Observer, complete = true)))

  class AddScalingTech extends Parallel(
    new If(
      new And(
        new MiningBasesAtLeast(2),
        new UnitsAtLeast(24, Protoss.Dragoon),
        new UnitsAtLeast(5, Protoss.Gateway, complete = true)),
      new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
    new If(
      new And(
        new GasPumpsAtLeast(3),
        new UnitsAtLeast(24, MatchWarriors)),
      new Parallel(
        new Build(Get(Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives),
          Get(Protoss.ZealotSpeed),
          Get(Protoss.PsionicStorm)))))

  class FinishDarkTemplarRush extends If(
    new And(
      new UnitsAtLeast(1, Protoss.TemplarArchives),
      new UnitsAtMost(0, Protoss.Observer),
      new UnitsAtMost(0, Protoss.Observatory)),
    new BuildOrder(Get(2, Protoss.DarkTemplar)))

  class NeedToCutWorkersForGateways extends And(
    new Latch(new And(new MiningBasesAtLeast(2), new EnemyBasesAtMost(1))),
    new UnitsAtMost(4, MatchOr(Protoss.Gateway, Protoss.RoboticsFacility)),
    new Not(new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.dtRush)))

  override def buildPlans: Seq[Plan] = Seq(
    new CapGasAt(500),
    new FinishDarkTemplarRush,
    new If(new Not(new NeedToCutWorkersForGateways), new Parallel(new WriteStatus("GatewayCut"), new PumpWorkers(maximumConcurrently = 1))),
    new Build(Get(Protoss.Pylon), Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.DragoonRange), Get(2, Protoss.Gateway)),

    // Detection
    new If(buildCannons, new BuildCannonsAtNatural(2)),
    new If(buildCannons, new BuildCannonsAtExpansions(1)),
    new GetReactiveObservers,

    // Expansions
    new If(new And(new MiningBasesAtLeast(2), new UnitsAtLeast(39, MatchWorker), new Check(() => With.blackboard.wantToAttack() && With.blackboard.safeToMoveOut())), new PylonBlock),
    new RequireMiningBases(2),
    new If(new And(new ReadyForThirdIfSafe, new SafeForThird), new RequireBases(3)),
    new Trigger(
      new BasesAtLeast(3),
      new Parallel(
        new PvPIdeas.TakeBase2,
        new PvPIdeas.TakeBase3WithGateways)),

    new AddScalingTech,
    new PvPIdeas.TrainArmy,

    // Three-base transition
    new If(
      new NeedToCutWorkersForGateways,
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Assimilator))),
    new AddEarlyTech,
    new RequireBases(3),

    new FlipIf(
      new SafeAtHome,
      new AddLateGateways,
      new AddLateTech),

    new RequireMiningBases(3),
    new If(new MiningBasesAtLeast(3), new Build((Get(12, Protoss.Gateway)))),
    new RequireMiningBases(4),
    new If(new MiningBasesAtLeast(4), new Build((Get(16, Protoss.Gateway)))),
  )
}

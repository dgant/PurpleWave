package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.PumpWorkers
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtNaturalAndExpansions
import Planning.Predicates.Compound.{And, Latch, Not, Sticky}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost, SafeToMoveOut}
import Planning.Predicates.Strategy.{EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchMobileDetectors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss

class PvPMidGame extends GameplanTemplate {

  override val completionCriteria: Predicate = new Latch(new MiningBasesAtLeast(3))

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val attackPlan: Plan = new Parallel(
    new If(
      new EnemiesAtMost(0, UnitMatchMobileDetectors),
      new Attack(Protoss.DarkTemplar)),
    new PvPIdeas.AttackSafely)

  override def archonPlan: Plan = new PvPIdeas.MeldArchonsPvP

  val goingTemplar = new Sticky(new UnitsAtLeast(1, Protoss.CitadelOfAdun))

  val buildCannons = new And(
    // Templar are reasonably likely
    new Or(
      new EnemyRecentStrategy(With.fingerprints.dtRush),
      new And(
        new EnemyBasesAtMost(1),
        new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.fourGateGoon)))),

    // We can't use a Robotics Facility
    // Note that this is deliberately placed after the Templar check so it only sticks at the point we're thinking about DTs
    new Sticky(new UnitsAtMost(0, Protoss.RoboticsFacility)))

  class EnemyPassiveOpening extends Or(
    new EnemyStrategy(With.fingerprints.robo),
    new EnemyBasesAtLeast(2))

  class AddTech extends If(
    goingTemplar,

    new Parallel(
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives)),
      new FlipIf(
        new EnemyPassiveOpening,
        new Build(
          Get(2, Protoss.Assimilator),
          Get(Protoss.PsionicStorm),
          Get(Protoss.ZealotSpeed)),
        new Build(Get(5, Protoss.Gateway))),
      new BuildGasPumps,
      new Build(
        Get(Protoss.GroundDamage),
        Get(7, Protoss.Gateway),
        Get(Protoss.HighTemplarEnergy),
        Get(2, Protoss.GroundDamage),
        Get(10, Protoss.Gateway))),

    new Parallel(
      new Build(
        Get(3, Protoss.Gateway),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory),
        Get(5, Protoss.Gateway)),
      new BuildGasPumps,
      new Build(
        Get(Protoss.RoboticsSupportBay),
        Get(Protoss.ShuttleSpeed),
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.ZealotSpeed),
        Get(7, Protoss.Gateway),
        Get(Protoss.ScarabDamage))))

  class GetReactiveObservers extends If(
    new And(
      new EnemyHasShown(Protoss.DarkTemplar),
      new Or(
        new Not(goingTemplar),
        new ReadyForThirdIfSafe)),
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
        new UnitsAtLeast(5, Protoss.Gateway))))

  class SafeForThird extends And(
    new SafeToMoveOut,
    new Or(
      new Not(new EnemyHasShown(Protoss.DarkTemplar)),
      new UnitsAtLeast(2, Protoss.Observer, complete = true)))

  class Expand extends RequireMiningBases(3)

  override def buildPlans: Seq[Plan] = Seq(
    new Build(Get(Protoss.Pylon), Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.DragoonRange), Get(2, Protoss.Gateway)),

    new If(buildCannons, new BuildCannonsAtNaturalAndExpansions(2)),

    new GetReactiveObservers,

    new RequireMiningBases(2),
    new If(new And(new ReadyForThirdIfSafe, new SafeForThird), new Expand),

    new PvPIdeas.TrainArmy,

    new AddTech,

    new PumpWorkers(oversaturate = true),

    new Expand
  )
}

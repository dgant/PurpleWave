package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Predicates.Economy.GasAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive._
import Planning.UnitMatchers.UnitMatchMobileDetectors
import ProxyBwapi.Races.Protoss

class PvPLateGame2 extends GameplanTemplate {

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  override def workerPlan: Plan = new Parallel(
    new If(new SafeAtHome, new PumpWorkers(true, cap = 42)),
    new PumpWorkers(false, cap = 75))

  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val attackPlan: Plan = new Parallel(
    new If(
      new EnemiesAtMost(0, UnitMatchMobileDetectors),
      new Attack(Protoss.DarkTemplar)),
    new PvPIdeas.AttackSafely)
  override def archonPlan: Plan = new PvPIdeas.MeldArchonsPvP

  class BuildTech extends Parallel(
    new Build(Get(Protoss.Pylon), Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.DragoonRange)),
    new Build(Get(3, Protoss.Gateway)),
    new RequireMiningBases(2),
    new Build(
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory),
      Get(5, Protoss.Gateway)),
    new If(new GasAtMost(800), new BuildGasPumps),
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives),
      Get(Protoss.ZealotSpeed),
      Get(Protoss.PsionicStorm),
      Get(Protoss.ObserverSpeed)),
    new UpgradeContinuously(Protoss.ShuttleSpeed))

  override val buildPlans = Vector(
    new PvPIdeas.TakeBase3,
    new PvPIdeas.TakeBase4,
    new PvPIdeas.TrainArmy,
    new BuildTech,
    new RequireMiningBases(2),
    new RequireBases(3),
    new Build(Get(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new PvPIdeas.ForgeUpgrades,
    new Build(Get(12, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(Get(20, Protoss.Gateway)),
  )
}
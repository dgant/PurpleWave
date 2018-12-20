package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2Gate1012Goon

class PvP2Gate1012Goon extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(PvP2Gate1012Goon)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Protoss.Nexus))
  override def priorityAttackPlan: Plan = new AttackWithDarkTemplar
  override def defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  override val defaultScoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)
  
  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.TwoGate1012
  override def buildPlans = Vector(
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)),

    new If(
      new EnemyStrategy(With.fingerprints.forgeFe),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay))),

    new If(
      new Or(
        new UnitsAtLeast(2, UnitMatchOr(Protoss.DarkTemplar, Protoss.Reaver), complete = true),
        new And(
          new UnitsAtLeast(12, UnitMatchWarriors),
          new UnitsAtLeast(1, Protoss.Forge),
          new SafeAtHome)),
      new RequireMiningBases(2)),

    new FlipIf(
      new SafeAtHome,
      new PvPIdeas.TrainArmy,
      new Parallel(
        new Build(Get(3, Protoss.Gateway)),
        new If(
          new And(
            new EnemyStrategy(With.fingerprints.fourGateGoon),
            new Not(new EnemyStrategy(With.fingerprints.robo)),
            new Not(new EnemyStrategy(With.fingerprints.twoGate))),
          new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives))))),

    new If(
      new SafeAtHome,
      new RequireMiningBases(2)),

    new Build(
      Get(Protoss.Forge),
      Get(4, Protoss.Gateway),
      Get(2, Protoss.Nexus)),
    new BuildCannonsAtNatural(2),
  )
}

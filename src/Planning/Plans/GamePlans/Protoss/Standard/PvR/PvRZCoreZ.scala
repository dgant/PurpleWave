package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.BuildRequests.BuildRequest
import Planning.Plans.Army.ConsiderAttacking
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.{If, Or}
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.Macro.Automatic.Pump
import Planning.Predicates.Compound.And
import Planning.Predicates.Strategy.{Employing, EnemyIsTerran, EnemyRaceKnown, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvROpenZCoreZ

class PvRZCoreZ extends GameplanTemplateVsRandom {
  
  override val activationCriteria: Predicate = new Employing(PvROpenZCoreZ)
  override val completionCriteria: Predicate = new Or(
    new UnitsAtLeast(2, UnitMatchWarriors),
    new And(
      new EnemyRaceKnown,
      new UnitsAtLeast(1, Protoss.CyberneticsCore)))

  override def attackPlan: Plan = new If(
    new Or(
      new EnemyIsTerran,
      new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.oneGateCore, With.fingerprints.nexusFirst)),
    new ConsiderAttacking)

  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.ZCoreZ

  override def buildPlans: Vector[Plan] = Vector(
    new DefendFightersAgainstRush,
    new Pump(Protoss.Dragoon)
  )
}

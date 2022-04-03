package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.Buildables.RequestProduction
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.Pump
import Planning.Predicates.Compound.{And, Or}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyIsTerran, EnemyRaceKnown, EnemyStrategy}
import Planning.UnitMatchers.MatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvROpenZCoreZ

class PvRZCoreZ extends GameplanTemplateVsRandom {
  
  override val activationCriteria: Predicate = new Employing(PvROpenZCoreZ)
  override val completionCriteria: Predicate = new Or(
    new UnitsAtLeast(2, MatchWarriors),
    new And(
      new EnemyRaceKnown,
      new UnitsAtLeast(1, Protoss.CyberneticsCore)))

  override def attackPlan: Plan = new If(
    new Or(
      new EnemyIsTerran,
      new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.oneGateCore, With.fingerprints.nexusFirst)),
    new ConsiderAttacking)

  override val buildOrder: Vector[RequestProduction] = ProtossBuilds.ZCoreZ

  override def buildPlans: Vector[Plan] = Vector(
    new Pump(Protoss.Dragoon)
  )
}

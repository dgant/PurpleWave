package Planning.Plans.GamePlans.Zerg.ZvE

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.UnitMatchers.UnitMatchOr
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{ExtractorTrick, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Matchup.EnemyIsTerran
import Planning.Predicates.Milestones.{EnemyUnitsAtMost, UnitsAtLeast}
import Planning.Predicates.Scenarios.EnemyStrategy
import Planning.Predicates.{Employing, StartPositionsAtLeast}
import Planning.Plans.Scouting.{FoundEnemyBase, Scout}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZvE4Pool

class Zerg4Pool extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(ZvE4Pool)
  
  override def defaultAggressionPlan: Plan = new If(
    new Latch(
      new And(
        new Check(() => With.self.supplyUsed >= 18),
        new UnitsAtLeast(3, Zerg.Larva)),
      GameTime(0, 10)()),
    new Aggression(99),
    new Aggression(1.5))
  
  override def defaultScoutPlan: Plan = new Parallel(
    new If(
      new And(
        new Or(
          new StartPositionsAtLeast(3),
          new Not(new EnemyIsTerran)),
        new Or(
          new Not(new FoundEnemyBase),
          new EnemyUnitsAtMost(0, UnitMatchOr(Protoss.PhotonCannon, Protoss.CyberneticsCore, Protoss.Assimilator, Protoss.Dragoon, Protoss.Corsair)))),
      new Scout(2) { scouts.get.unitMatcher.set(Zerg.Overlord) }),
    new If(
      new And(
        new Latch(
          new And(
            new MineralsAtLeast(126),
            new UnitsAtLeast(1, Zerg.SpawningPool))),
        new Not(new EnemyStrategy(With.fingerprints.twoGate)),
        new UnitsAtLeast(4, UnitMatchOr(Zerg.Drone, Zerg.Extractor))),
      new Scout))
  
  override def defaultSupplyPlan: Plan = NoPlan()
  
  override def defaultAttackPlan: Plan = new Attack
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => {
      With.blackboard.gasTargetRatio = 0
      With.blackboard.gasLimitFloor = 0
      With.blackboard.gasLimitCeiling = 0
    }),
  
    new Pump(Zerg.Drone, 3),
    
    new BuildOrder(
      Get(1, Zerg.SpawningPool),
      Get(5, Zerg.Drone)),
    new Build(Get(1, Zerg.Overlord)),
    
    new ExtractorTrick,
    new Pump(Zerg.Zergling),
    new If(
      new And(
        new MineralsAtLeast(450),
        new UnitsAtLeast(3, Zerg.Drone)),
      new RequireMiningBases(2))
  )
}

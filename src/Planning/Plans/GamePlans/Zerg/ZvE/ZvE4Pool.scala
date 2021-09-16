package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Aggression, AllInIf, Attack}
import Planning.Plans.Basic.{NoPlan, Write}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, ExtractorTrick, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutNow
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.MatchOr
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZvE4Pool
import Utilities.Time.Seconds

class ZvE4Pool extends GameplanTemplate {
  
  override val activationCriteria = new Employing(ZvE4Pool)
  
  override def aggressionPlan: Plan = new If(
    new Latch(
      new And(
        new Check(() => With.self.supplyUsed >= 18),
        new UnitsAtLeast(3, Zerg.Larva)),
      Seconds(10)()),
    new Aggression(99),
    new Aggression(1.5))
  
  override def scoutPlan: Plan = new If(
    new And(
      new Latch(
        new And(
          new MineralsAtLeast(126),
          new UnitsAtLeast(1, Zerg.SpawningPool))),
      new Not(new EnemyStrategy(With.fingerprints.twoGate)),
      new UnitsAtLeast(4, MatchOr(Zerg.Drone, Zerg.Extractor))),
    new ScoutNow)
  
  override def supplyPlan: Plan = NoPlan()
  
  override def attackPlan: Plan = new Attack
  
  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAt(0, 0),
    new Write(With.blackboard.pushKiters, true),
    new AllInIf(new EnemiesAtLeast(1, Protoss.PhotonCannon)),
  
    new Pump(Zerg.Drone, 3),
    
    new BuildOrder(
      Get(Zerg.SpawningPool),
      Get(5, Zerg.Drone)),
    new Build(Get(Zerg.Overlord)),
    
    new ExtractorTrick,
    new Pump(Zerg.Zergling),
    new If(
      new And(
        new MineralsAtLeast(450),
        new UnitsAtLeast(3, Zerg.Drone)),
      new RequireMiningBases(2))
  )
}

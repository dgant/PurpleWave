package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Army._
import Planning.Plans.Basic.{NoPlan, Write}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Placement.BuildBunkersAtEnemy
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{EnemiesAtLeast, FoundEnemyBase, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Terran.TvE.TvE2RaxSCVMarine
import Utilities.Time.Seconds
import Utilities.UnitCounters.CountExcept
import Utilities.UnitFilters.{IsRecruitableForCombat, IsWorker}

class TvE2RaxSCVMarine extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvE2RaxSCVMarine)

  override def scoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutAt(10))

  class ReadyToAttack extends Latch(new Or(
    new UnitsAtLeast(26, IsRecruitableForCombat),
    new And(
      new UnitsAtLeast(2, Terran.Marine),
      new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.nexusFirst, With.fingerprints.twelveHatch))))

  override def aggressionPlan: Plan = new If(
    new ReadyToAttack,
    new If(
      new Or(
        new MineralsAtLeast(400),
        new EnemiesAtLeast(1, Terran.Bunker),
        new EnemiesAtLeast(1, Protoss.PhotonCannon),
        new EnemiesAtLeast(1, Zerg.SunkenColony)),
      new AllInIf,
      new Aggression(2.0)),
    super.aggressionPlan)

  override def attackPlan: Plan = new If(
    new ReadyToAttack,
    new Parallel(
      new Delay(Seconds(7)(), new AttackAndHarass),
      new AttackWithWorkers(new CountExcept(4, IsWorker))))

  override def supplyPlan: Plan = NoPlan()

  override def workerPlan: Plan = NoPlan()

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency,
    new TerranReactionVsWorkerRush
  )
  
  override val buildOrder = Vector(
    Get(9, Terran.SCV),
    Get(1, Terran.SupplyDepot),
    Get(10, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(11, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(13, Terran.SCV),
    Get(1, Terran.Marine),
    Get(2, Terran.SupplyDepot))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Write(With.blackboard.pushKiters, true),
    new Pump(Terran.Marine),
    new BuildOrder(Get(20, Terran.SCV)),
    new Pump(Terran.SCV, 3),
    new If(
      new ReadyToAttack,
      new If(new FoundEnemyBase, new BuildBunkersAtEnemy(1)))
  )
}

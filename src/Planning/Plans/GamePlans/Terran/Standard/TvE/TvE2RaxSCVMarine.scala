package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, AllIn, Attack, EjectScout}
import Planning.Plans.Basic.{NoPlan, Write}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Placement.BuildBunkersAtEnemy
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{EnemiesAtLeast, FoundEnemyBase, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.{UnitMatchMobile, UnitMatchWorkers}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Terran.TvE.TvE2RaxSCVMarine

class TvE2RaxSCVMarine extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvE2RaxSCVMarine)

  override def initialScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutAt(10))

  class ReadyToAttack extends Latch(new Or(
    new UnitsAtLeast(26, UnitMatchMobile),
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
      new AllIn,
      new Aggression(2.0)),
    super.aggressionPlan)

  override def attackPlan: Plan = new If(
    new ReadyToAttack,
    new Parallel(
      new Delay(GameTime(0, 7)(), new Attack),
      new Attack(UnitMatchWorkers, new UnitCountExcept(4, UnitMatchWorkers))))

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
    new DefendFightersAgainstRush,
    new Pump(Terran.Marine),
    new BuildOrder(Get(20, Terran.SCV)),
    new Pump(Terran.SCV, 3),
    new If(
      new ReadyToAttack,
      new If(new FoundEnemyBase, new BuildBunkersAtEnemy(1)),
      new EjectScout)
  )
}

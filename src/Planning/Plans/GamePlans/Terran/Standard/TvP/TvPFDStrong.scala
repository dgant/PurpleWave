package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Milestones.{BasesAtLeast, EnemiesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchSiegeTank
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvPFDStrong

class TvPFDStrong extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvPFDStrong)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override def defaultScoutPlan: Plan = new ScoutAt(13)
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Terran.Vulture),
    new Trigger(
      new UnitsAtLeast(2, UnitMatchSiegeTank, complete = true),
      super.defaultAttackPlan))
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def defaultWorkerPlan: Plan = TvPIdeas.workerPlan

  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(
      Get(10, Terran.SCV),
      Get(Terran.SupplyDepot)),
    new Trigger(
      new MineralsAtLeast(250), // Keep that worker mining as long as possible
      new BuildOrder(
        Get(Terran.Barracks),
        Get(Terran.Refinery)),
      // Don't build this, just hold minerals for it
      new BuildOrder(Get(2, Terran.CommandCenter))),
    new BuildOrder(
      Get(14, Terran.SCV),
      Get(Terran.Factory),
      Get(15, Terran.SCV),
      Get(2,  Terran.SupplyDepot),
      Get(1,  Terran.Marine),
      Get(16, Terran.SCV),
      Get(2,  Terran.Marine),
      Get(17, Terran.SCV),
      Get(3,  Terran.SupplyDepot)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new And(
        new UnitsAtMost(0, Terran.Factory, complete = true),
        new Or(
          new GasAtLeast(100),
          new UnitsAtLeast(1, Terran.Factory))),
      new CapGasWorkersAt(1)),
    new If(
      new And(
        new UnitsAtMost(0, Terran.MachineShop),
        new Or(
          new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway),
          new EnemiesAtLeast(1, Protoss.Zealot))),
      new PumpMatchingRatio(Terran.Vulture, 1, 2, Seq(Enemy(Protoss.Zealot, 0.5)))),

    new Pump(Terran.MachineShop, 1),
    new BuildOrder(Get(2, Terran.SiegeTankUnsieged)),
    new Pump(Terran.Marine),
    new Build(Get(Terran.SpiderMinePlant)),
    new Pump(Terran.Vulture),
    new Build(Get(Terran.SiegeMode)),
    new RequireBases(2),
    new Build(
      Get(2, Terran.Factory),
      Get(Terran.EngineeringBay)),
  )
}

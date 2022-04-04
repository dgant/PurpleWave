package Planning.Plans.GamePlans.Terran.Standard.TvT

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.{AttackAndHarass, FloatBuildings}
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{BasesAtLeast, EnemiesAtLeast, UnitsAtLeast}
import Planning.Predicates.Reactive.EnemyBasesAtMost
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT2FacTanks

class TvT2FacTanks extends GameplanTemplate {

  override val activationCriteria = new Employing(TvT2FacTanks)
  override val completionCriteria = new Latch(new BasesAtLeast(2))

  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(
      Get(9, Terran.SCV),
      Get(Terran.SupplyDepot),
      Get(12, Terran.SCV)),
    new Trigger(
      new MineralsAtLeast(250), // Keep that worker mining as long as possible
      new BuildOrder(
        Get(Terran.Barracks),
        Get(Terran.Refinery)),
      // Don't build this, just hold minerals for it
      new BuildOrder(Get(2, Terran.CommandCenter))),
    new BuildOrder(
      Get(16, Terran.SCV),
      Get(2, Terran.SupplyDepot),
      Get(Terran.Factory),
      Get(18, Terran.SCV),
      Get(2, Terran.Factory)))

  override def attackPlan: Plan = new AttackAndHarass

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs),
      new Parallel(
        new PumpRatio(Terran.Vulture, 0, 3, Seq(Enemy(Terran.Marine, 1.0))),
        new PumpRatio(Terran.Marine, 0, 8, Seq(Enemy(Terran.Marine, 2.0))))),
    new Pump(Terran.MachineShop),
    new If(
      new UnitsAtLeast(4, IsTank),
      new Build(Get(Terran.SiegeMode))),
    new PumpRatio(Terran.Goliath, 0, 8, Seq(Enemy(Terran.Vulture, 1.0), Enemy(Terran.Wraith, 2.9))),
    new If(
      new EnemiesAtLeast(1, Terran.Wraith),
      new UpgradeContinuously(Terran.GoliathAirRange)),
    new Pump(Terran.SiegeTankUnsieged),
    new If(
      new EnemyBasesAtMost(1),
      new Build(Get(Terran.Armory))),
    new Trigger(
      new UnitsAtLeast(2, IsTank),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(1, Terran.Factory, complete = true),
      new FloatBuildings(Terran.Barracks, Terran.EngineeringBay))
  )
}

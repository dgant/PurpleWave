package Planning.Plans.GamePlans.Terran.TvP

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.RepairBunker
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Plans.Placement.{BuildBunkersAtNatural, BuildMissileTurretsAtNatural}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.{TvP1RaxFE, TvP2FacJoyO, TvPFDStrong, TvPSiegeExpandBunker}

class TvP1RaxFE extends GameplanTemplate {

  override val activationCriteria = new Or(
    new Employing(TvP1RaxFE),
    new Latch(
      new And(
        new Employing(TvPSiegeExpandBunker, TvPFDStrong, TvP2FacJoyO),
        new EnemyStrategy(With.fingerprints.gasSteal, With.fingerprints.forgeFe, With.fingerprints.nexusFirst))))

  override val completionCriteria = new Latch(new And(
    new BasesAtLeast(2),
    new TechStarted(Terran.SiegeMode),
    new UnitsAtLeast(1, Terran.EngineeringBay)
  ))

  override def scoutPlan: Plan = new ScoutOn(Terran.SupplyDepot)

  override def attackPlan = new If(
    new EnemyStrategy(With.fingerprints.nexusFirst),
    new ConsiderAttacking)

  override def buildOrderPlan = new Parallel(
    new BuildOrder(
      Get(9,  Terran.SCV),
      Get(Terran.SupplyDepot),
      Get(11, Terran.SCV),
      Get(Terran.Barracks),
      Get(15, Terran.SCV),
      Get(2,  Terran.CommandCenter),
      Get(Terran.Marine)),
    new If(new EnemiesAtLeast(1, Protoss.Zealot), new BuildBunkersAtNatural(1)),
    new BuildOrder(
      Get(2, Terran.SupplyDepot),
      Get(Terran.Refinery)),
    new If(
      new Not(new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)),
      new Pump(Terran.Marine, 4)),
    new FlipIf(
      new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway),
      new Build(
        Get(18, Terran.SCV),
        Get(Terran.Factory)),
      new BuildBunkersAtNatural(1)))

  override def buildPlans: Seq[Plan] = Seq(
    new RepairBunker,
    new PopulateBunkers,

    new CapGasAt(250),

    new TvPIdeas.ReactiveEarlyVulture,
    new Pump(Terran.SiegeTankUnsieged),
    new Build(Get(Terran.MachineShop)),

    new If(
      new EnemyDarkTemplarLikely,
      new BuildMissileTurretsAtNatural(1)),

    new Trigger(
      new EnemiesAtLeast(2, Protoss.Dragoon),
      new Build(
        Get(Terran.SiegeMode),
        Get(2, Terran.Factory),
        Get(2, Terran.Refinery),
        Get(2, Terran.MachineShop))),

    new BuildMissileTurretsAtNatural(1),
    new Build(
      Get(2, Terran.Factory),
      Get(2, Terran.Refinery),
      Get(Terran.SiegeMode)),

    new Pump(Terran.Marine, 5)
  )
}

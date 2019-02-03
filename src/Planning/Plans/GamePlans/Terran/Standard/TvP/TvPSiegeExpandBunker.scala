package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, EjectScout, RecruitFreelancers}
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.Situational.RepairBunker
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Terran._
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitCounters.UnitCountExactly
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvPSiegeExpandBunker

class TvPSiegeExpandBunker extends GameplanTemplate {

  override val activationCriteria = new Employing(TvPSiegeExpandBunker)
  override val completionCriteria = new Latch(new And(
    new BasesAtLeast(2),
    new TechStarted(Terran.SiegeMode),
    new UnitsAtLeast(1, Terran.EngineeringBay)
  ))

  override def scoutPlan: Plan = new ScoutOn(Terran.Factory)

  override def attackPlan = new If(
    new EnemyStrategy(With.fingerprints.nexusFirst),
    new ConsiderAttacking)

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9,  Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks),
    Get(12, Terran.SCV),
    Get(Terran.Refinery),
    Get(14, Terran.SCV),
    Get(2,  Terran.SupplyDepot),
    Get(Terran.Marine),
    Get(15, Terran.SCV),
    Get(Terran.Factory),
    Get(16, Terran.SCV),
    Get(2,  Terran.Marine),
    Get(17, Terran.SCV),
    Get(3,  Terran.Marine),
    Get(18, Terran.SCV))

  override def buildPlans: Seq[Plan] = Seq(

    new RepairBunker,

    new TvPIdeas.CutGasDuringFactory,
    new TvPIdeas.ReactiveEarlyVulture,

    new If(
      new EnemyStrategy(With.fingerprints.nexusFirst),
      new RequireMiningBases(2)),
    new If(
      new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway),
      new BuildBunkersAtMain(1)),

    new If(
      new And(
        new EnemiesAtMost(2, Protoss.Dragoon),
        new EnemyStrategy(With.fingerprints.nexusFirst)),
      new Parallel(
        new BuildBunkersAtEnemyNatural(1),
        new RecruitFreelancers(Terran.SCV, UnitCountExactly(5))),
      new Parallel(
        new EjectScout,
        new PopulateBunkers,
        new BuildBunkersAtNatural(1))),

    new If(
      new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.nexusFirst),
      new Pump(Terran.Marine),
      new Pump(Terran.Marine, 4)),

    new Build(Get(Terran.MachineShop)),
    new FlipIf(
      new UnitsAtLeast(1, Terran.Bunker, complete = true),
      new Parallel(
        new BuildOrder(
          Get(Terran.SiegeTankUnsieged),
          Get(Terran.EngineeringBay)),
        new Pump(Terran.SiegeTankUnsieged),
        new Build(Get(Terran.SiegeMode))),
      new RequireMiningBases(2)),

    new FlipIf(
      new EnemiesAtLeast(2, Protoss.Dragoon),
      new BuildMissileTurretsAtNatural(1),
      new Build(
        Get(2, Terran.Factory),
        Get(2, Terran.Refinery),
        Get(2, Terran.MachineShop)))
  )
}

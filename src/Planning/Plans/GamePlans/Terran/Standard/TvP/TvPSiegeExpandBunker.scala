package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, EjectScout, RecruitFreelancers}
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Enemy, Pump, PumpMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Terran.{BuildBunkersAtEnemyNatural, BuildBunkersAtMain, BuildBunkersAtNatural, BuildMissileTurretsAtNatural}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitCounters.UnitCountExactly
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvPSiegeExpandBunker

class TvPSiegeExpandBunker extends GameplanModeTemplate {

  override val activationCriteria = new Employing(TvPSiegeExpandBunker)
  override val completionCriteria = new Latch(new And(
    new BasesAtLeast(2),
    new TechStarted(Terran.SiegeMode),
    new UnitsAtLeast(1, Terran.EngineeringBay)
  ))

  override def defaultScoutPlan: Plan = new ScoutOn(Terran.Factory)

  override def defaultAttackPlan = new If(
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
          new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.nexusFirst),
          new EnemiesAtLeast(1, Protoss.Zealot))),
      new PumpMatchingRatio(Terran.Vulture, 1, 2, Seq(Enemy(Protoss.Zealot, 0.5)))),

    new If(
      new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway),
      new BuildBunkersAtMain(1)),

    new If(
      new And(
        new UnitsAtMost(2, Protoss.Dragoon),
        new EnemyStrategy(With.fingerprints.nexusFirst)),
      new Parallel(
        new BuildBunkersAtEnemyNatural(1),
        new RecruitFreelancers(Terran.SCV, UnitCountExactly(5))),
      new Parallel(
        new EjectScout,
        new BuildBunkersAtNatural(1))),

    new If(
      new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.nexusFirst),
      new Pump(Terran.Marine),
      new Pump(Terran.Marine, 4)),

    new FlipIf(
      new UnitsAtLeast(1, Terran.Bunker, complete = true),
      new Parallel(
        new BuildOrder(
          Get(Terran.MachineShop),
          Get(Terran.SiegeTankUnsieged),
          Get(Terran.EngineeringBay)),
        new Pump(Terran.SiegeTankUnsieged),
        new Build(Get(Terran.SiegeMode))),
      new RequireMiningBases(2)),

    new BuildMissileTurretsAtNatural(1),
    new Build(
      Get(2, Terran.Factory),
      Get(2, Terran.MachineShop))
  )

}

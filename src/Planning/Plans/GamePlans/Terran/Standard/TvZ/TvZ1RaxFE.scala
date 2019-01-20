package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Terran.BuildBunkersAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ1RaxFE

class TvZ1RaxFE extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvZ1RaxFE)
  override val completionCriteria: Predicate = new Latch(new MiningBasesAtLeast(2))

  override def attackPlan: Plan = NoPlan()
  override def scoutPlan: Plan = new If(
    new Not(new EnemyStrategy(With.fingerprints.fourPool)),
    new If(
      new StartPositionsAtLeast(3),
      new ScoutOn(Terran.Barracks, scoutCount = 2),
      new ScoutOn(Terran.Barracks)))

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks),
    Get(15, Terran.SCV))

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency
  )

  override def buildPlans: Seq[Plan] = Seq(
    new DefendFightersAgainstEarlyPool,
    new If(
      new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch),
      new RequireMiningBases(2)),
    new Pump(Terran.Marine),
    new If(
      new EnemyStrategy(With.fingerprints.ninePool, With.fingerprints.overpool),
      new Build(Get(2, Terran.Barracks))),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new Build(Get(4, Terran.Barracks))),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.ninePool, With.fingerprints.overpool, With.fingerprints.fourPool),
        new UnitsAtLeast(2, Terran.Barracks, complete = true)),
      new BuildBunkersAtNatural(1)),
    new RequireMiningBases(2),
    new Build(
      Get(Terran.Refinery),
      Get(Terran.EngineeringBay),
      Get(Terran.BioDamage),
      Get(Terran.Academy))
  )
}
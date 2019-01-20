package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{Latch, Not}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{MiningBasesAtLeast, TechStarted, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ2RaxAcademy

class TvZ2RaxAcademy extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvZ2RaxAcademy)
  override val completionCriteria: Predicate = new Latch(new MiningBasesAtLeast(2))

  override def attackPlan: Plan = new Trigger(new UnitsAtLeast(1, Terran.Medic, complete = true), new Attack)
  override def scoutPlan: Plan = new If(
    new Not(new EnemyStrategy(With.fingerprints.fourPool)),
    new If(
      new StartPositionsAtLeast(3),
      new ScoutOn(Terran.Barracks, scoutCount = 2),
      new ScoutOn(Terran.Barracks)))

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency
  )

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(Terran.Barracks),
    Get(13, Terran.SCV),
    Get(Terran.Marine),
    Get(2, Terran.SupplyDepot),
    Get(2, Terran.Barracks),
    Get(14, Terran.SCV),
    Get(2, Terran.Marine),
    Get(15, Terran.SCV),
    Get(Terran.Refinery),
    Get(3, Terran.Marine),
    Get(Terran.Academy))

  override def buildPlans: Seq[Plan] = Seq(
    new DefendFightersAgainstEarlyPool,
    new TechContinuously(Terran.Stim),
    new PumpRatio(Terran.Medic, 2, 6, Seq(Friendly(Terran.Marine, 0.2))),
    new PumpRatio(Terran.Firebat, 0, 2, Seq(Friendly(Terran.Marine, 0.1))),
    new Pump(Terran.Marine),
    new Trigger(
      new Or(
        new TechStarted(Terran.Stim),
        new GasAtLeast(100)),
      new CapGasWorkersAt(2)),
    new If(
      new TechStarted(Terran.Stim),
      new RequireMiningBases(2)),
    new Build(Get(Terran.MarineRange)),
  )
}
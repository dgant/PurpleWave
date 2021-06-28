package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackSafely
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.{BuildBunkersAtMain, BuildBunkersAtNatural, BuildMissileTurretsAtNatural}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones.{EnemyHasShown, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitMatchers.{MatchOr, MatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran.TvZ2RaxAcademy

class TvZ2RaxAcademy extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvZ2RaxAcademy)
  override val completionCriteria: Predicate = new Latch(new MiningBasesAtLeast(2))

  override def attackPlan: Plan = new Trigger(new UnitsAtLeast(1, Terran.Firebat, complete = true), new AttackSafely)
  override def scoutPlan: Plan = new If(
    new Not(new EnemyStrategy(With.fingerprints.fourPool)),
    new If(
      new StartPositionsAtLeast(3),
      new ScoutOn(Terran.Barracks, quantity = 1, scoutCount = 2),
      new ScoutOn(Terran.Barracks, quantity = 1)))

  override def workerPlan: Plan = NoPlan()

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency,
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.ninePool),
        new UnitsAtLeast(2, Terran.Barracks)),
      new BuildBunkersAtMain(1)))

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks),
    Get(13, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(14, Terran.SCV),
    Get(Terran.Marine),
    Get(2, Terran.SupplyDepot),
    Get(15, Terran.SCV),
    Get(2, Terran.Marine),
    Get(16, Terran.SCV),
    Get(Terran.Refinery),
    Get(3, Terran.Marine),
    Get(Terran.Academy),
    Get(17, Terran.SCV),
    Get(5, Terran.Marine),
    Get(18, Terran.SCV),
    Get(3, Terran.SupplyDepot),
    Get(7, Terran.Marine),
    Get(Terran.Stim),
    Get(Terran.Comsat),
    Get(9, Terran.Marine),
    Get(2, Terran.Medic),
    Get(19, Terran.SCV),
    Get(2, Terran.Firebat),
    Get(20, Terran.SCV),
    Get(11, Terran.Marine),
    Get(21, Terran.SCV),
    Get(13, Terran.Marine))

  class NeedTurret extends Or(
    new EnemyHasShown(Zerg.HydraliskDen),
    new EnemyHasShown(Zerg.Hydralisk),
    new EnemyHasShown(Zerg.LurkerEgg),
    new EnemyHasShown(Zerg.Lurker))

  override def buildPlans: Seq[Plan] = Seq(
    new FlipIf(
      new And(
        new SafeAtHome,
        new Not(new NeedTurret)),
      new Parallel(
        new If(new NeedTurret, new BuildMissileTurretsAtNatural(1)),
        new PumpWorkers(oversaturate = true),
        new If(
          new UnitsAtMost(2, MatchWarriors),
          new Pump(Terran.Firebat, 2)),
        new PumpRatio(Terran.Medic, 2, 6, Seq(Friendly(MatchOr(Terran.Marine, Terran.Firebat), 0.2))),
        new PumpRatio(Terran.Firebat, 0, 2, Seq(Friendly(Terran.Marine, 0.1))),
        new Pump(Terran.Marine),
        // Hack to force placement of emergency bunkers in the main, not the natural
        new Trigger(
          new UnitsAtLeast(1, Terran.Academy),
          new BuildBunkersAtNatural(1))),
      new RequireMiningBases(2))
  )
}
package Planning.Plans.GamePlans.Zerg.ZvT

import Macro.Requests.{RequestBuildable, Get}
import Planning.Plans.Army.{AllInIf, AttackAndHarass}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases}
import Planning.Plans.Placement.{BuildSunkensAtExpansions, BuildSunkensAtNatural}
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemyWalledIn, UpgradeComplete}
import Planning.Predicates.Strategy.Employing
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT2HatchLingBustMuta
import Utilities.Time.Seconds

class ZvT2HatchLingBustMuta extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT2HatchLingBustMuta)
  override def scoutPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Trigger(
    new UpgradeComplete(Zerg.ZerglingSpeed, 1, Seconds(10)()),
    new AttackAndHarass)

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese,
    new ZergReactionVsWorkerRush
  )

  // Based on Effort vs. Flash's 1-1-1:
  // https://www.youtube.com/watch?v=3sb47YGI7l8&feature=youtu.be&t=2280
  // https://docs.google.com/spreadsheets/d/1m6nU6FewJBC2LGQX_DPuo4PqzxH8hF3bazp8T6QlqRs/edit#gid=1166229923
  override def buildOrder: Seq[RequestBuildable] = Seq(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(12, Zerg.Drone),
    Get(2, Zerg.Hatchery),
    Get(Zerg.Extractor),
    Get(Zerg.SpawningPool),
    Get(17, Zerg.Drone),
    Get(Zerg.ZerglingSpeed),
    Get(Zerg.Lair),
    Get(4, Zerg.Zergling),
    Get(3, Zerg.Overlord),
    Get(20, Zerg.Zergling),
    Get(Zerg.Spire),
    Get(26, Zerg.Zergling)
  )

  override def buildPlans: Seq[Plan] = Seq(
    new Pump(Zerg.Mutalisk),
    new If(
      new EnemiesAtLeast(1, Terran.Vulture),
      new Parallel(
        new AllInIf(new Not(EnemyWalledIn)),
        new BuildSunkensAtNatural(1),
        new BuildSunkensAtExpansions(1)
      )),
    new Pump(Zerg.Drone, 20),
    new RequireBases(3),
    new Pump(Zerg.Drone, 24),
    new BuildGasPumps,
    new RequireBases(4),
  )
}

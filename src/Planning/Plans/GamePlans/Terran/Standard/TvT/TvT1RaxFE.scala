package Planning.Plans.GamePlans.Terran.Standard.TvT

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Requests.{RequestBuildable, Get}
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Plans.Placement.BuildBunkersAtNatural
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT1RaxFE

class TvT1RaxFE extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvT1RaxFE)
  override val completionCriteria: Predicate = new Latch(new Or(
    new UnitsAtLeast(1, Terran.Bunker, complete = true),
    new And(
      new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE),
      new UnitsAtLeast(1, Terran.Factory))))

  override def scoutPlan = new ScoutAt(12)
  override def attackPlan = new If(new EnemyStrategy(With.fingerprints.fourteenCC), new AttackAndHarass)

  val naturalZone: Zone = With.geography.ourNatural.zone

  override def buildOrder: Seq[RequestBuildable] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks),
    Get(15, Terran.SCV),
    Get(2, Terran.CommandCenter),
    Get(Terran.Refinery),
    Get(Terran.Marine),
    Get(2, Terran.SupplyDepot),
    Get(16, Terran.SCV),
    Get(2, Terran.Marine),
    Get(18, Terran.SCV),
    Get(3, Terran.Marine),
    Get(Terran.Factory))

  override def buildPlans: Seq[Plan] = Seq(
    new PopulateBunkers,
     new If(
      new Not(new EnemyStrategy(With.fingerprints.fourteenCC, With.fingerprints.oneRaxFE)),
      new Parallel(
        new BuildBunkersAtNatural(1),
        new Build(Get(Terran.Bunker)))), // Forces completion of the Bunker
    new Pump(Terran.Marine),
    new Pump(Terran.Vulture)
  )
}

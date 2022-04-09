package Planning.Plans.GamePlans.Terran.TvT

import Lifecycle.With
import Macro.Requests.{RequestBuildable, Get}
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones.{BasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT1FacPort

class TvT1FacPort extends GameplanTemplate {

  override val activationCriteria = new Employing(TvT1FacPort)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override def scoutPlan = new ScoutAt(13)
  override def attackPlan = new Parallel(
    new If(new EnemyStrategy(With.fingerprints.fourteenCC), new AttackAndHarass),
    new Trigger(
      new UnitsAtLeast(1, Terran.Wraith, complete = true),
      new AttackAndHarass))


  override def buildOrder: Seq[RequestBuildable] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(12, Terran.SCV),
    Get(Terran.Barracks),
    Get(Terran.Refinery),
    Get(15, Terran.SCV),
    Get(Terran.Marine),
    Get(Terran.Factory),
    Get(17, Terran.SCV),
    Get(2, Terran.SupplyDepot),
    Get(18, Terran.SCV),
    Get(2, Terran.Marine),
    Get(Terran.MachineShop),
    Get(22, Terran.SCV),
    Get(Terran.Starport))

  override def buildPlans: Seq[Plan] = Seq(
    new Pump(Terran.ControlTower),
    new TechContinuously(Terran.WraithCloak),
    new Pump(Terran.Wraith),
    new Pump(Terran.SiegeTankUnsieged),
    new Trigger(
      new UnitsAtLeast(1, Terran.Wraith),
      new RequireMiningBases(2)),
    new TechContinuously(Terran.SiegeMode),
    new Pump(Terran.Vulture)
  )
}

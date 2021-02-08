package Planning.Plans.GamePlans.Terran.Standard.TvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{Attack, FloatBuildings}
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump}
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT1FacFE

class TvT1FacFE extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvT1FacFE)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Terran.Factory))

  override def scoutPlan = new ScoutAt(13)
  override def attackPlan = new If(new EnemyStrategy(With.fingerprints.fourteenCC), new Attack)

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(12, Terran.SCV),
    Get(Terran.Barracks),
    Get(Terran.Refinery),
    Get(15, Terran.SCV),
    Get(Terran.Marine),
    Get(2, Terran.SupplyDepot),
    Get(Terran.Factory),
    Get(18, Terran.SCV),
    Get(2, Terran.Marine),
    Get(2, Terran.CommandCenter),
    Get(Terran.MachineShop),
    Get(19, Terran.SCV),
    Get(3, Terran.SupplyDepot),
    Get(22, Terran.SCV),
    Get(Terran.SiegeTankUnsieged),
    Get(2, Terran.Factory))

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new And(
        new UnitsAtLeast(1, Terran.Factory),
        new UnitsAtMost(0, Terran.Factory, complete = true)),
      new CapGasWorkersAt(2)),
    new Pump(Terran.SiegeTankUnsieged),
    new If(
      new UnitsAtLeast(1, Terran.Vulture),
      new FloatBuildings(Terran.Barracks, Terran.EngineeringBay)),
  )
}

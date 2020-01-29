package Planning.Plans.GamePlans.Terran.Standard.TvT

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, ConsiderAttacking, FloatBuildings}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT2Base2Port

class TvT2Base2Port extends GameplanTemplate {

  override val activationCriteria = new Employing(TvT2Base2Port)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(3))

  override def attackPlan: Plan = new Parallel(
    new Attack(Terran.Wraith),
    new ConsiderAttacking)

  override def workerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers(oversaturate = false))

  override def buildPlans: Seq[Plan] = Seq(
    new PopulateBunkers,
    new RequireMiningBases(2),
    new BuildGasPumps,
    new Build(
      Get(2, Terran.Starport),
      Get(Terran.MachineShop),
      Get(Terran.ControlTower),
      Get(Terran.WraithCloak)),
    new PumpRatio(Terran.SiegeTankUnsieged, 0, 3, Seq(Enemy(Terran.Goliath, 1.0))),
    new Pump(Terran.Wraith),
    new Pump(Terran.SiegeTankUnsieged),
    new Build(Get(Terran.Academy)),
    new If(
      new TechComplete(Terran.WraithCloak),
      new RequireMiningBases(3)),
    new Build(Get(3, Terran.Factory)),
    new Pump(Terran.Vulture),
    new FloatBuildings
  )
}

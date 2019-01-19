package Planning.Plans.GamePlans.Terran.Standard.TvT

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Compound.{FlipIf, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT2Port

class TvT2Port extends GameplanModeTemplate {

  override val activationCriteria = new Employing(TvT2Port)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(2))

  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Terran.Wraith),
    new ConsiderAttacking)

  override def defaultWorkerPlan: Plan = new PumpWorkers(oversaturate = false)

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks),
    Get(12, Terran.SCV),
    Get(Terran.Refinery),
    Get(13, Terran.SCV),
    Get(2, Terran.SupplyDepot),
    Get(16, Terran.SCV),
    Get(Terran.Factory),
    Get(20, Terran.SCV),
    Get(2, Terran.Starport),
    Get(Terran.Vulture)
  )
  override def buildPlans: Seq[Plan] = Seq(
    new FlipIf(
      new UnitsAtLeast(6, Terran.Wraith),
      new Pump(Terran.Wraith),
      new Build(
        Get(Terran.ControlTower),
        Get(Terran.WraithCloak),
        Get(2, Terran.CommandCenter))),
    new Pump(Terran.Vulture)
  )
}

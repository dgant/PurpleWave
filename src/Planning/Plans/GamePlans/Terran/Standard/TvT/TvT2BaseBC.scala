package Planning.Plans.GamePlans.Terran.Standard.TvT

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Plans.Placement.BuildMissileTurretsAtBases
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT2BaseBC

class TvT2BaseBC extends GameplanTemplate {

  override val activationCriteria = new Employing(TvT2BaseBC)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(3))

  override def attackPlan: Plan = new If(new UnitsAtLeast(1, Terran.Battlecruiser, complete = true), new Attack)

  override def workerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers(oversaturate = true))

  override def buildPlans: Seq[Plan] = Seq(
    new PopulateBunkers,
    new RequireMiningBases(2),
    new BuildGasPumps,
    new If(
      new UnitsAtLeast(4, Terran.Battlecruiser),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtMost(0, Terran.ScienceFacility),
      new Pump(Terran.Wraith)),
    new If(
      new EnemyHasShownWraithCloak,
      new Parallel(
        new BuildMissileTurretsAtBases(2),
        new Pump(Terran.ScienceVessel, 1))),
    new Pump(Terran.Battlecruiser),
    new Pump(Terran.SiegeTankUnsieged, 3),
    new Pump(Terran.Vulture),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Factory),
      Get(Terran.Starport),
      Get(Terran.ScienceFacility),
      Get(2, Terran.Starport),
      Get(Terran.PhysicsLab),
      Get(2, Terran.ControlTower)),
    new Pump(Terran.SiegeTankUnsieged),
    new Build(
      Get(3, Terran.Starport),
      Get(3, Terran.ControlTower),
      Get(Terran.Academy)),
  )
}

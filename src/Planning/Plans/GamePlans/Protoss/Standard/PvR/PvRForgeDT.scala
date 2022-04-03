package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.Buildables.{RequestProduction, Get}
import Planning.Plan
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR1BaseDT

class PvRForgeDT extends GameplanTemplateVsRandom {

  override val activationCriteria = new Employing(PvR1BaseDT)
  override def scoutPlan = NoPlan()

  override def attackPlan = new If(new UnitsAtLeast(6, Protoss.Gateway, complete = true), new ConsiderAttacking)

  override lazy val blueprints = Vector(
    new Blueprint(Protoss.Pylon,        placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Pylon,        placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Pylon,        placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(Protoss.Forge,        placement = Some(PlacementProfiles.tech)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Gateway,      placement = Some(PlacementProfiles.backPylon)))

  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new Parallel(
        new Build(Get(2, Protoss.PhotonCannon)),
        new PumpWorkers,
        new Build(Get(6, Protoss.PhotonCannon)))))

  override def buildOrder: Seq[RequestProduction] = Vector(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(9, Protoss.Probe),
    Get(Protoss.Forge),
    Get(11, Protoss.Probe),
    Get(2, Protoss.PhotonCannon),
    Get(13, Protoss.Probe),
    Get(3, Protoss.PhotonCannon),
    Get(14, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(15, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(16, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(Protoss.CyberneticsCore),
    Get(18, Protoss.Probe),
    Get(Protoss.Zealot),
    Get(4, Protoss.PhotonCannon))

  override def buildPlans = Vector(
    new CapGasAt(300),
    new Pump(Protoss.DarkTemplar, 4),
    new Trigger(
      new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true),
      new RequireMiningBases(2)),
    new Trigger(
      new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true),
      new Parallel(
        new Build(Get(Protoss.DragoonRange)),
        new Pump(Protoss.Dragoon))),
    new Pump(Protoss.Zealot),
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives)),
    new Build(Get(6, Protoss.PhotonCannon)),
    new Build(Get(3, Protoss.Gateway)),
    new If(
      new BasesAtLeast(2),
      new Build(Get(8, Protoss.Gateway)))
  )
}

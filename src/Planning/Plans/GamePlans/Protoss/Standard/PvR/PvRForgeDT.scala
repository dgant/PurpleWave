package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
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

  override def attackPlan = new Parallel(
    new Attack(Protoss.DarkTemplar),
    new If(new UnitsAtLeast(6, Protoss.Gateway, complete = true), new ConsiderAttacking)
  )

  override lazy val blueprints = Vector(
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.Pylon)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.Pylon)),
    new Blueprint(this, placement = Some(PlacementProfiles.backPylon),    building = Some(Protoss.Pylon)),
    new Blueprint(this, placement = Some(PlacementProfiles.tech),         building = Some(Protoss.Forge)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.PhotonCannon)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.PhotonCannon)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.PhotonCannon)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.PhotonCannon)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.PhotonCannon)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall),  building = Some(Protoss.PhotonCannon)),
    new Blueprint(this, placement = Some(PlacementProfiles.backPylon),    building = Some(Protoss.Gateway)))


  override def emergencyPlans: Seq[Plan] = Seq(
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new Parallel(
        new Build(Get(2, Protoss.PhotonCannon)),
        new PumpWorkers,
        new Build(Get(6, Protoss.PhotonCannon)))))

  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(9, Protoss.Probe),
    Get(Protoss.Forge),
    Get(11, Protoss.Probe),
    Get(2, Protoss.PhotonCannon),
    Get(13, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(15, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(16, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(4, Protoss.PhotonCannon))

  override def buildPlans = Vector(
    new CapGasAt(300),
    new DefendFightersAgainstEarlyPool,
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

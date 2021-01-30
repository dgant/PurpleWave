package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.EjectScout
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.ProposePlacement
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemiesAtMost, FoundEnemyBase, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchMobileDetectors
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvE.PvTProxyDarkTemplar

class ProxyDarkTemplarRush extends GameplanTemplate {

  override val activationCriteria = new Employing(PvTProxyDarkTemplar)

  // Might be the fastest possible DT rush.
  // An example: https://youtu.be/ca40eQ1s7iw
  override def placementPlan: Plan = new ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(Protoss.Pylon,   placement = Some(PlacementProfiles.proxyPylon),    preferZone = ProxyPlanner.proxyMiddle),
      new Blueprint(Protoss.Gateway, placement = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyMiddle),
      new Blueprint(Protoss.Gateway, placement = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyMiddle))
  }

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(8,  Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(11, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(Protoss.Zealot),
    Get(Protoss.CitadelOfAdun),
    Get(2,  Protoss.Zealot),
    Get(2,  Protoss.Pylon),
    Get(Protoss.TemplarArchives),
    Get(2,  Protoss.Gateway),
    Get(15, Protoss.Probe),
    Get(4, Protoss.Gateway))

  override def initialScoutPlan: Plan = new If(new Not(new FoundEnemyBase), new ScoutOn(Protoss.Pylon))
  override def attackPlan: Plan = new Trigger(new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true), super.attackPlan)
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()

  private class EmergencyDragoon extends If(
    new EnemiesAtLeast(1, Terran.Vulture),
    new Build(
      Get(1, Protoss.Dragoon),
      Get(Protoss.DragoonRange)))

  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new If(
      new EnemiesAtMost(0, UnitMatchMobileDetectors),
      new Parallel(
        new Pump(Protoss.DarkTemplar, maximumTotal = 3),
        new EmergencyDragoon,
        new Pump(Protoss.DarkTemplar, maximumTotal = 6)),
    new EmergencyDragoon),

    super.supplyPlan,
    new PumpWorkers,
    new RequireMiningBases(2),
    new UpgradeContinuously(Protoss.DragoonRange),
    new Pump(Protoss.Dragoon),
    new BuildGasPumps,
    new RequireMiningBases(3),
    new Build(Get(5, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(Get(7, Protoss.Gateway)),
    new RequireMiningBases(5),
    new Build(Get(20, Protoss.Gateway))
  )
}

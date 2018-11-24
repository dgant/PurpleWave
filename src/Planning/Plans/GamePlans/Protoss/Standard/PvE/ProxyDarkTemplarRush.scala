package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemiesAtMost}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchMobileDetectors
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvE.ProxyDarkTemplar

class ProxyDarkTemplarRush extends GameplanModeTemplate {

  override val activationCriteria = new Employing(ProxyDarkTemplar)

  // Might be the fastest possible DT rush.
  // An example: https://youtu.be/ca40eQ1s7iw

  override def defaultPlacementPlan: Plan = new ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(this, building = Some(Protoss.Pylon)),
      new Blueprint(this, building = Some(Protoss.Gateway)),
      new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.proxyPylon),    preferZone = ProxyPlanner.proxyAutomaticSneaky),
      new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky),
      new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky))
  }

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(1, Protoss.Nexus),
    Get(8, Protoss.Probe),
    Get(1, Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(1, Protoss.Gateway),
    Get(11, Protoss.Probe),
    Get(1, Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(1, Protoss.CyberneticsCore),
    Get(1, Protoss.Zealot),
    Get(1, Protoss.CitadelOfAdun),
    Get(2, Protoss.Zealot),
    Get(2, Protoss.Pylon),
    Get(1, Protoss.TemplarArchives),
    Get(3, Protoss.Gateway),
    Get(15, Protoss.Probe),
    Get(4, Protoss.Gateway))


  override def defaultScoutPlan: Plan = new If(new Not(new FoundEnemyBase), new ScoutAt(11))
  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)
  override def defaultWorkerPlan: Plan = NoPlan()
  override def defaultSupplyPlan: Plan = NoPlan()

  private class EmergencyDragoon extends If(
    new EnemiesAtLeast(1, Terran.Vulture),
    new Build(
      Get(1, Protoss.Dragoon),
      Get(Protoss.DragoonRange)))

  override def buildPlans: Seq[Plan] = Seq(

    new If(
      new EnemiesAtMost(0, UnitMatchMobileDetectors),
      new Parallel(
        new Pump(Protoss.DarkTemplar, maximumTotal = 3),
        new EmergencyDragoon,
        new Pump(Protoss.DarkTemplar, maximumTotal = 6)),
    new EmergencyDragoon),

    super.defaultSupplyPlan,
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

package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.PvZIdeas.{Eject4PoolScout, MeldArchonsUntilStorm}
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.BuildCannonsAtNatural
import Planning.Plans.Scouting.{MonitorBases, ScoutOn}
import Planning.Predicates.Compound.{Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZ1BaseForgeTech

class PvZ1BaseForgeTech extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvZ1BaseForgeTech)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))

  override def initialScoutPlan: Plan = new ScoutOn(Protoss.Pylon)

  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(Protoss.Pylon,        placement = Some(PlacementProfiles.hugTownHallTowardsEntrance)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHallTowardsEntrance)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHallTowardsEntrance)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHallTowardsEntrance)),
    new Blueprint(Protoss.PhotonCannon, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Pylon,        placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Forge,        placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Protoss.Gateway,      placement = Some(PlacementProfiles.hugTownHall)))

  override def attackPlan: Plan = new Parallel(
    new MonitorBases(Protoss.Corsair, initialUnitCounter = UnitCountEverything),
    super.attackPlan)

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvZIdeas.ConditionalDefendFFEWithProbesAgainst4Pool,
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new Parallel(
        new Build(
          Get(Protoss.Forge),
          Get(Protoss.PhotonCannon)),
        new Pump(Protoss.Probe, 12),
        new Build(Get(4, Protoss.PhotonCannon)))))

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(9, Protoss.Probe),
    Get(Protoss.Forge),
    Get(11, Protoss.Probe),
    Get(2, Protoss.PhotonCannon),
    Get(13, Protoss.Probe),
    Get(3, Protoss.PhotonCannon),
    Get(15, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(16, Protoss.Probe),
    Get(Protoss.Gateway))

  override def archonPlan: Plan = new MeldArchonsUntilStorm

  override def buildPlans: Seq[Plan] = Seq(
    new Eject4PoolScout,
    new BuildOrder(
      Get(12, Protoss.Probe),
      Get(2, Protoss.PhotonCannon)),

    // Key army
    // We can spend all our gas here but need minerals to expand
    new FlipIf(
      new EnemyHasShown(Zerg.Mutalisk),
      new Parallel(
        new Pump(Protoss.DarkTemplar, 2),
        new If(
          new Or(new UpgradeComplete(Protoss.GroundDamage), new UnitsAtLeast(1, Protoss.DarkTemplar)),
          new Pump(Protoss.Zealot, 6),
          new Pump(Protoss.Zealot, 12))),
      new Parallel(
        new PumpRatio(Protoss.Corsair, 4, 12, Seq(Enemy(Zerg.Mutalisk, 1.5))),
        new If(new Or(new EnemyHasShown(Zerg.Mutalisk), new UpgradeStarted(Protoss.ZealotSpeed)), new Pump(Protoss.HighTemplar)),
        new If(new Not(new EnemyHasShown(Zerg.Hydralisk)), new Build(Get(Protoss.Scout))))),

    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Stargate),
      Get(4, Protoss.PhotonCannon)),

    new BuildOrder(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives),
      Get(Protoss.DarkTemplar),
      Get(Protoss.GroundDamage),
      Get(Protoss.ZealotSpeed)),
    new If(new EnemyHasShown(Zerg.Mutalisk), new UpgradeContinuously(Protoss.AirDamage)),

    new If(
      new Or(
        new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
        new UnitsAtLeast(12, UnitMatchWarriors)),
      new Parallel(
        new BuildCannonsAtNatural(2),
        new RequireMiningBases(2))),

    new If(
      new MiningBasesAtLeast(2),
      new Build(
        Get(5, Protoss.Gateway),
        Get(2, Protoss.Assimilator)),
      new Build(
        Get(3, Protoss.Gateway))),

    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot),

    new Build(Get(5, Protoss.Gateway))
  )
}

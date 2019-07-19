package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{Attack, Hunt}
import Planning.Plans.Compound.{FlipIf, If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.{Scout, ScoutOn}
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchOr
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZ1BaseForgeTech, PvZ1BaseForgeTechForced}

class PvZ1BaseForgeTech extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvZ1BaseForgeTech, PvZ1BaseForgeTechForced)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))

  override def scoutPlan: Plan = new ScoutOn(Protoss.Pylon)

  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, placement = Some(PlacementProfiles.hugTownHall)))

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)
  override def attackPlan: Plan = new Parallel(
    new Hunt(Zerg.Overlord, Protoss.Corsair),
    new Scout(100) { scouts().unitMatcher.set(Protoss.Corsair) },
    new Attack(Protoss.Corsair),
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
    Get(Protoss.Gateway)
  )

  override def buildPlans: Seq[Plan] = Seq(
    new BuildOrder(
      Get(12, Protoss.Probe),
      Get(2, Protoss.PhotonCannon)),
    new Pump(Protoss.DarkTemplar, 2),
    new Pump(Protoss.Zealot, 12),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Stargate),
      Get(4, Protoss.PhotonCannon)),
    new PumpRatio(Protoss.Corsair, 6, 18, Seq(Enemy(Zerg.Mutalisk, 1.5))),
    new Parallel(
      new If(
        new EnemiesAtMost(0, UnitMatchOr(Zerg.Hydralisk, Zerg.SporeColony))),
        new Build(Get(Protoss.Scout))),
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives)),
    new FlipIf(
      new UnitsAtLeast(1, Protoss.DarkTemplar),
      new Build(
        Get(3, Protoss.Gateway),
        Get(Protoss.GroundDamage),
        Get(Protoss.ZealotSpeed)),
      new Parallel(
        new Trigger(
          new UnitsAtLeast(1, Protoss.TemplarArchives),
          new BuildCannonsAtNatural(2)),
        new RequireMiningBases(2))),
    new Build(
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator),
      Get(Protoss.PsionicStorm)),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot)
  )
}

package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Milestones.{UnitsAtLeast, UpgradeComplete}
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyIsTerran, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR2Gate4Gate

class PvR2Gate4Gate extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvR2Gate4Gate)

  override lazy val blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Protoss.Pylon), placement = Some(PlacementProfiles.hugTownHall)))

  override def attackPlan: Plan = new Trigger(
    new Or(
      new UpgradeComplete(Protoss.DragoonRange),
      new EnemyBasesAtLeast(2),
      new EnemyIsTerran),
    new Attack)
  override def scoutPlan: Plan = new ScoutOn(Protoss.Gateway, 2)

  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.TwoGate910

  override def buildPlans = Vector(
    new DefendFightersAgainstEarlyPool,
    new CapGasAt(400),

    new BuildOrder(Get(5, Protoss.Zealot)),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new Pump(Protoss.Zealot, 7)),
    new UpgradeContinuously(Protoss.DragoonRange),
    new Pump(Protoss.Dragoon),

    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange),
      Get(4, Protoss.Gateway)),

    new If(
      new UnitsAtLeast(4, Protoss.Gateway),
      new Pump(Protoss.Zealot))
  )
}

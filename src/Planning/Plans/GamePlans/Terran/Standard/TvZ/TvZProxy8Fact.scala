package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Micro.Agency.Intention
import Planning.Plans.Army.{Aggression, Attack, RecruitFreelancers}
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.And
import Planning.Predicates.Economy.{GasAtLeast, SupplyBlocked}
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.{UnitMatchCustom, UnitMatchWorkers}
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran.TvZ.TvZProxy8Fact

class TvZProxy8Fact extends GameplanTemplate {

  override val activationCriteria = new Employing(TvZProxy8Fact)
  
  override def aggressionPlan: Plan = new Aggression(1.5)
  
  override def attackPlan: Plan = new Parallel(new Attack, new Attack(Terran.SCV))
  override def scoutPlan: Plan = new ScoutOn(Terran.SCV, quantity = 9)
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()

  override def placementPlan: Plan = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(building = Some(Terran.Factory),  preferZone = ProxyPlanner.proxyAutomaticAggressive, respectHarvesting = Some(false), placement = Some(PlacementProfiles.proxyBuilding)))
  }
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    Get(8, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(1, Terran.Refinery))

  class Runby extends Plan {
    val lock: LockUnits = new LockUnits {
      unitMatcher.set(UnitMatchCustom(u => u.is(Terran.Vulture) && ! u.base.exists(b => b.isStartLocation)))
    }

    override def onUpdate(): Unit = {
      val base = With.geography.enemyBases.filter(_.isStartLocation)
      if (base.isEmpty) return
      lock.acquire(this)
      lock.units.foreach(_.agent.intend(this, new Intention {
        toTravel = Some(base.head.heart.pixelCenter)
        canFlee = false
        canAttack = false
      }))
    }
  }
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),

    new If(
      new And(
        new Or(
          new GasAtLeast(100),
          new UnitsAtLeast(1, Terran.Factory))),
      new CapGasAt(0),
      new CapGasAt(100)),

    new If(
      new EnemiesAtLeast(1, Zerg.SunkenColony, complete = true),
      new Runby),

    new If(
      new UnitsAtLeast(1, Terran.Refinery),
      new Build(Get(1, Terran.Factory))),

    new Trigger(
      new UnitsAtLeast(1, Terran.Factory, complete = false),
      new Parallel(
        new Build(
          Get(1, Terran.SupplyDepot),
          Get(10, Terran.SCV)),
        new Pump(Terran.Vulture),
        new If(
          new SupplyBlocked,
          new RequireSufficientSupply),
        new Pump(Terran.Marine),
        new PumpWorkers,
        new RecruitFreelancers(UnitMatchWorkers, new UnitCountExcept(8, UnitMatchWorkers))))
  )
}
package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Latch}
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtNatural, BuildCannonsInMain}
import Planning.Predicates.Milestones.{EnemiesAtMost, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2GateDTExpand

class PvP2GateDarkTemplar extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvP2GateDTExpand)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(2))
  override val defaultWorkerPlan  = NoPlan()
  override val defaultScoutPlan   = new ScoutOn(Protoss.CyberneticsCore)
  override val defaultAttackPlan  = new Trigger(
    new Or(
      new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
      new EnemyStrategy(With.fingerprints.nexusFirst)),
    new Attack)
  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone)))

  override val buildOrder = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gateway_Dark_Templar_(vs._Protoss)
    // We get gas/core faster because of mineral locking + later scout
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),            // 8
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),          // 10
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Pylon),            // 11
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),           // 13 = 11 + Z
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),      // 16 = 14 + Z
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),  // 18 = 16 + Z
    Get(17,  Protoss.Probe),
    Get(2,   Protoss.Zealot),           // 21 = 17 + ZZ
    Get(18,  Protoss.Probe),
    Get(3,   Protoss.Pylon),            // 22 = 18 + ZZ
    Get(20,  Protoss.Probe),            // 24 = 20 + ZZ
    Get(1,   Protoss.CitadelOfAdun),
    Get(1,   Protoss.Dragoon),          // 26 = 20 + ZZ + D
    Get(21,  Protoss.Probe),
    Get(2,   Protoss.Dragoon),          // 29 = 21 + ZZ + DD
    Get(2,   Protoss.Gateway),
    Get(3,   Protoss.Pylon),
    Get(1,   Protoss.TemplarArchives),
    Get(22,  Protoss.Probe),            // 30 = 22 + ZZZZ + DD
    Get(4,   Protoss.Pylon),            // 32 = 22 + ZZZZ + DD
    Get(23,  Protoss.Probe),
    Get(2,   Protoss.DarkTemplar),
    Get(24,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(25,  Protoss.Probe),
    Get(5,   Protoss.Pylon))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE
  )
  
  override val buildPlans = Vector(
    new EjectScout,
    new RequireSufficientSupply,
    new If(
      new And(
        new EnemiesAtMost(0, Protoss.Observer),
        new EnemiesAtMost(0, Protoss.Observatory),
        new UnitsAtMost(2, Protoss.DarkTemplar)),
        new Pump(Protoss.DarkTemplar, 3, 1)),
    new PumpWorkers,
    new If(
      new EnemyDarkTemplarLikely,
      new Parallel(
        new BuildCannonsInMain(1),
        new BuildCannonsAtNatural(3)),
      new BuildCannonsAtNatural(2)),
    new Pump(Protoss.Dragoon),
    new RequireMiningBases(2))
}

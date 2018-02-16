package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtNatural, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtMost, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen2GateDTExpand

class PvPOpen2GateDarkTemplar extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvPOpen2GateDTExpand)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val defaultWorkerPlan  = NoPlan()
  override val scoutAt            = 14
  override val defaultAttackPlan  = new Trigger(new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true), initialAfter = new Attack)
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
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),           // 10
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),       // 11
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),            // 13
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),            // 19 = 17 + Z
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),             // 22 = 18 + ZZ
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),           // 23 = 19 + ZZ
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),     // 26 = 20 + ZZ + D
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),           // 27 = 21 + ZZ + D
    RequestAtLeast(2,   Protoss.Gateway),           // 29 = 21 + ZZ + DD
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.TemplarArchives),
    RequestAtLeast(4,   Protoss.Zealot),            // 33 = 21 + ZZZZ + DD
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),             // 34 = 22 + ZZZZ + DD
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.DarkTemplar),
    RequestAtLeast(24,  Protoss.Probe))
  
  override val buildPlans = Vector(
    new RequireSufficientSupply,
    new If(
      new And(
        new EnemyUnitsAtMost(0, Protoss.Observer),
        new EnemyUnitsAtMost(0, Protoss.Observatory),
        new UnitsAtMost(2, Protoss.DarkTemplar)),
        new TrainContinuously(Protoss.DarkTemplar, 3, 1)),
    new TrainContinuously(Protoss.Dragoon),
    new TrainWorkersContinuously,
    new BuildCannonsAtNatural(2),
    new RequireMiningBases(2))
}

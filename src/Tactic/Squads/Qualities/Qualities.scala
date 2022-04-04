package Tactic.Squads.Qualities

import Utilities.UnitFilters.{IsCombatSpellcaster, IsWorker}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object Qualities {
  object Cloaked extends Quality {
    def apply(u: UnitInfo): Boolean = u.burrowed || u.isAny(
      Terran.Ghost, Terran.Wraith, Terran.SpiderMine,
      Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer,
      Zerg.Lurker, Zerg.LurkerEgg) || (Terran.Vulture(u) && u.player.hasTech(Terran.SpiderMinePlant))
    override val counteredBy: Array[Quality] = Array(Detector)
  }
  object SpiderMine extends Quality {
    override def apply(u: UnitInfo): Boolean = Terran.SpiderMine(u)
    override val counteredBy: Array[Quality] = Array(AntiVulture)
  }
  object AntiSpiderMine extends Quality {
    override def apply(u: UnitInfo): Boolean = u.canAttackGround && (
      u.flying
      || u.unitClass.floats
      || u.damageOnHitGround >= Terran.SpiderMine.maxHitPoints
      || u.pixelRangeGround > 32.0 * 3.0)
    override val counteredBy: Array[Quality] = Array.empty
    @inline override def counterScaling: Double = 5.0
  }
  object Vulture extends Quality {
    override def apply(u: UnitInfo): Boolean = Terran.Vulture(u)
    override val counteredBy: Array[Quality] = Array(AntiVulture)
  }
  object AntiVulture extends Quality {
    override def apply(u: UnitInfo): Boolean = (AntiGround(u) && ! u.isAny(Protoss.Zealot, Protoss.DarkTemplar, Protoss.Scout, Protoss.Arbiter, Protoss.Carrier, Zerg.Zergling))
    override val counteredBy: Array[Quality] = Array.empty
  }
  object Air extends Quality {
    def apply(u: UnitInfo): Boolean = u.flying
    override val counteredBy: Array[Quality] = Array(AntiAir)
  }
  object Ground extends Quality {
    def apply(u: UnitInfo): Boolean = ! u.flying && ! Terran.SpiderMine(u)
    override val counteredBy: Array[Quality] = Array(AntiGround)
  }
  object AirCombat extends Quality {
    def apply(u: UnitInfo): Boolean = u.flying
    override val counteredBy: Array[Quality] = Array(AntiAirCombat)
  }
  object GroundCombat extends Quality {
    def apply(u: UnitInfo): Boolean = ! u.flying && ! Terran.SpiderMine(u)
    override val counteredBy: Array[Quality] = Array(AntiGroundCombat)
  }
  object AntiAir extends Quality {
    def apply(u: UnitInfo): Boolean = IsCombatSpellcaster(u) || u.canAttackAir
  }
  object AntiGround extends Quality {
    def apply(u: UnitInfo): Boolean = IsCombatSpellcaster(u) || (u.canAttackGround && ! u.unitClass.isWorker)
  }
  object AntiAirCombat extends Quality {
    def apply(u: UnitInfo): Boolean = u.canAttackAir && ! u.isAny(Terran.Ghost, Protoss.Arbiter)
  }
  object AntiGroundCombat extends Quality {
    def apply(u: UnitInfo): Boolean = u.canAttackGround && ! u.isAny(Terran.Ghost, Protoss.Arbiter, IsWorker)
  }
  object Combat extends Quality {
    def apply(u: UnitInfo): Boolean = (u.canAttack && ! u.unitClass.isWorker)
    override val counteredBy: Array[Quality] = Array(Combat)
  }
  object Detector extends Quality {
    def apply(u: UnitInfo): Boolean = u.unitClass.isDetector
    @inline override def counterScaling: Double = 5.0
  }
  object StaticDefense extends Quality {
    def apply(u: UnitInfo): Boolean = u.unitClass.attacksGround && u.unitClass.isBuilding
  }
  object AntiStaticDefense extends Quality {
    def apply(u: UnitInfo): Boolean = u.pixelRangeGround > 32.0 * 7.0 || Terran.SiegeTankUnsieged(u)
  }
  val enemy: Array[Quality] = Array(
    Cloaked,
    SpiderMine,
    Vulture,
    Air,
    Ground,
    AirCombat,
    GroundCombat,
    StaticDefense
  )
  val friendly: Array[Quality] = Array(
    Detector,
    AntiSpiderMine,
    AntiVulture,
    AntiAir,
    AntiGround,
    AntiAirCombat,
    AntiGroundCombat,
    AntiStaticDefense
  )
}
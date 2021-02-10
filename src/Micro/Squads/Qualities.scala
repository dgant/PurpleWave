package Micro.Squads

import Planning.UnitMatchers.{MatchCombatSpellcaster, MatchWorkers}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object Qualities {
  object Cloaked extends Quality {
    def apply(u: UnitInfo): Boolean = u.burrowed || u.isAny(
      Terran.Ghost, Terran.Wraith, Terran.SpiderMine,
      Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer,
      Zerg.Lurker, Zerg.LurkerEgg) || (u.is(Terran.Vulture) && u.player.hasTech(Terran.SpiderMinePlant))
    override val counteredBy: Array[Quality] = Array(Detector)
  }
  object SpiderMine extends Quality {
    override def apply(u: UnitInfo): Boolean = u.is(Terran.SpiderMine)
    override val counteredBy: Array[Quality] = Array(AntiVulture)
  }
  object AntiSpiderMine extends Quality {
    override def apply(u: UnitInfo): Boolean = u.attacksAgainstGround > 0 && (
      u.flying
      || u.unitClass.floats
      || u.damageOnHitGround >= Terran.SpiderMine.maxHitPoints
      || u.pixelRangeGround > 32.0 * 3.0)
    override val counteredBy: Array[Quality] = Array.empty
    @inline override def counterScaling: Double = 5.0
  }
  object Vulture extends Quality {
    override def apply(u: UnitInfo): Boolean = u.is(Terran.Vulture)
    override val counteredBy: Array[Quality] = Array(AntiVulture)
  }
  object AntiVulture extends Quality {
    override def apply(u: UnitInfo): Boolean = (AntiGround.apply(u)
      && ! u.isAny(Protoss.Zealot, Protoss.DarkTemplar, Protoss.Scout, Protoss.Arbiter, Protoss.Carrier, Zerg.Zergling))
    override val counteredBy: Array[Quality] = Array.empty
  }
  object Air extends Quality {
    def apply(u: UnitInfo): Boolean = u.flying
    override val counteredBy: Array[Quality] = Array(AntiAir)
  }
  object Ground extends Quality {
    def apply(u: UnitInfo): Boolean = ! u.flying && ! u.isAny(Terran.SpiderMine)
    override val counteredBy: Array[Quality] = Array(AntiGround)
  }
  object AirCombat extends Quality {
    def apply(u: UnitInfo): Boolean = u.flying
    override val counteredBy: Array[Quality] = Array(AntiAirCombat)
  }
  object GroundCombat extends Quality {
    def apply(u: UnitInfo): Boolean = ! u.flying && ! u.isAny(Terran.SpiderMine)
    override val counteredBy: Array[Quality] = Array(AntiGroundCombat)
  }
  object AntiAir extends Quality {
    def apply(u: UnitInfo): Boolean = u.is(MatchCombatSpellcaster) || u.attacksAgainstAir > 0
  }
  object AntiGround extends Quality {
    def apply(u: UnitInfo): Boolean = u.is(MatchCombatSpellcaster) || (u.attacksAgainstGround > 0 && ! u.unitClass.isWorker)
  }
  object AntiAirCombat extends Quality {
    def apply(u: UnitInfo): Boolean = u.attacksAgainstAir > 0 && ! u.isAny(Terran.Ghost, Protoss.Arbiter)
  }
  object AntiGroundCombat extends Quality {
    def apply(u: UnitInfo): Boolean = u.attacksAgainstGround > 0 && ! u.isAny(Terran.Ghost, Protoss.Arbiter, MatchWorkers)
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
    def apply(u: UnitInfo): Boolean = u.pixelRangeGround > 32.0 * 7.0 || u.is(Terran.SiegeTankUnsieged)
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
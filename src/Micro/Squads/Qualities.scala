package Micro.Squads

import Planning.UnitMatchers.{UnitMatchCombatSpellcaster, UnitMatchWorkers}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

object Qualities {
  object Cloaked extends Quality {
    def accept(u: UnitInfo): Boolean = u.burrowed || u.isAny(
      Terran.Ghost, Terran.Wraith, Terran.SpiderMine,
      Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer,
      Zerg.Lurker, Zerg.LurkerEgg) || (u.is(Terran.Vulture) && u.player.hasTech(Terran.SpiderMinePlant))
    override val counteredBy: Array[Quality] = Array(Detector)
  }
  object SpiderMine extends Quality {
    override def accept(u: UnitInfo): Boolean = u.is(Terran.SpiderMine)
    override val counteredBy: Array[Quality] = Array(AntiVulture)
  }
  object AntiSpiderMine extends Quality {
    override def accept(u: UnitInfo): Boolean = u.attacksAgainstGround > 0 && (
      u.flying
      || u.unitClass.floats
      || u.damageOnHitGround >= Terran.SpiderMine.maxHitPoints
      || u.pixelRangeGround > 32.0 * 3.0)
    override val counteredBy: Array[Quality] = Array.empty
    override def counterScaling(input: Double): Double = 5.0 * input
  }
  object Vulture extends Quality {
    override def accept(u: UnitInfo): Boolean = u.is(Terran.Vulture)
    override val counteredBy: Array[Quality] = Array(AntiVulture)
  }
  object AntiVulture extends Quality {
    override def accept(u: UnitInfo): Boolean = (AntiGround.accept(u)
      && ! u.isAny(Protoss.Zealot, Protoss.DarkTemplar, Protoss.Scout, Protoss.Arbiter, Protoss.Carrier, Zerg.Zergling))
    override val counteredBy: Array[Quality] = Array.empty
  }
  object FlyingBuilding extends Quality {
    def accept(u: UnitInfo): Boolean = u.unitClass.isFlyingBuilding
  }
  object Air extends Quality {
    def accept(u: UnitInfo): Boolean = u.flying
    override val counteredBy: Array[Quality] = Array(AntiAir)
  }
  object Ground extends Quality {
    def accept(u: UnitInfo): Boolean = ! u.flying && ! u.isAny(Terran.SpiderMine)
    override val counteredBy: Array[Quality] = Array(AntiGround)
  }
  object AirCombat extends Quality {
    def accept(u: UnitInfo): Boolean = u.flying
    override val counteredBy: Array[Quality] = Array(AntiAirCombat)
  }
  object GroundCombat extends Quality {
    def accept(u: UnitInfo): Boolean = ! u.flying && ! u.isAny(Terran.SpiderMine)
    override val counteredBy: Array[Quality] = Array(AntiGroundCombat)
  }
  object AntiAir extends Quality {
    def accept(u: UnitInfo): Boolean = u.is(UnitMatchCombatSpellcaster) || u.attacksAgainstAir > 0
  }
  object AntiGround extends Quality {
    def accept(u: UnitInfo): Boolean = u.is(UnitMatchCombatSpellcaster) || (u.attacksAgainstGround > 0 && ! u.unitClass.isWorker)
  }
  object AntiAirCombat extends Quality {
    def accept(u: UnitInfo): Boolean = u.attacksAgainstAir > 0 && ! u.isAny(Terran.Ghost, Protoss.Arbiter)
  }
  object AntiGroundCombat extends Quality {
    def accept(u: UnitInfo): Boolean = u.attacksAgainstGround > 0 && ! u.isAny(Terran.Ghost, Protoss.Arbiter, UnitMatchWorkers)
  }
  object Combat extends Quality {
    def accept(u: UnitInfo): Boolean = (u.canAttack && ! u.unitClass.isWorker)
    override val counteredBy: Array[Quality] = Array(Combat)
  }
  object Detector extends Quality {
    def accept(u: UnitInfo): Boolean = u.unitClass.isDetector
    override def counterScaling(input: Double): Double = 5.0 * input
  }
  object SiegeTank extends Quality {
    def accept(u: UnitInfo): Boolean = u.unitClass.isSiegeTank
  }
  object Transport extends Quality {
    def accept(u: UnitInfo): Boolean = u.isAny(Terran.Dropship, Protoss.Shuttle, Zerg.Overlord)
  }
  object Transportable extends Quality {
    def accept(u: UnitInfo): Boolean = u.isAny(Protoss.HighTemplar, Protoss.Reaver, Zerg.Defiler)
  }
  val threats: Array[Quality] = Array(
    Cloaked,
    SpiderMine,
    Vulture,
    Air,
    Ground,
    AirCombat,
    GroundCombat
  )
  val answers: Array[Quality] = Array(
    Detector,
    AntiSpiderMine,
    AntiVulture,
    AntiAir,
    AntiGround,
    AntiAirCombat,
    AntiGroundCombat,
  )
  val roles: Array[Quality] = Array(
    FlyingBuilding,
    SiegeTank,
    Transport,
    Transportable
  )
}
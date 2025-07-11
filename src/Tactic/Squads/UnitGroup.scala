package Tactic.Squads

import Debugging.EnumerateUnits
import Information.Battles.Types.{Battle, GroupCentroid}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?
import Utilities.UnitFilters.{IsTank, UnitFilter}

import scala.collection.mutable

trait UnitGroup {
  def groupUnits        : Seq[UnitInfo]
  def groupOrderable    : Seq[UnitInfo] = groupUnits.view.filter(_.unitClass.orderable)
  def mobileUnits       : Seq[UnitInfo] = _mobileUnits()
  def attackers         : Seq[UnitInfo] = _attackers()
  def detectors         : Seq[UnitInfo] = _detectors().view
  def mobileDetectors   : Seq[UnitInfo] = _mobileDetectors()
  def arbiters          : Seq[UnitInfo] = _arbiters().view
  def repairables       : Seq[UnitInfo] = _repairables().view
  def repairableVIPs    : Seq[UnitInfo] = _repairableVIPs().view
  def warriorsCasters   : Seq[UnitInfo] = groupOrderable.view.filter(u => u.unitClass.isWarrior || u.unitClass.castsSpells)
  def attackersBio      : Seq[UnitInfo] = groupOrderable.view.filter(_.isAny(Terran.Marine, Terran.Firebat))
  def attackersCloaky   : Seq[UnitInfo] = groupOrderable.view.filter(u => u.isAny(Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Protoss.DarkTemplar, Zerg.Lurker))
  def battles           : Set[Battle]   = _battles()
  def battleEnemies     : Set[UnitInfo] = _battleEnemies()
  def roadblocks        : Set[UnitInfo] = _roadblocks()
  def warriorsCastersCount      : Int       = _warriorsCastersCount()
  def attackersBioCount         : Int       = _attackersBioCount()
  def stormCount                : Int       = _stormCount()
  def airToAirStrength          : Int       = _airToAirStrength()
  def attacksAir                : Boolean   = _attacksAir()
  def attacksGround             : Boolean   = _attacksGround()
  def catchesAir                : Boolean   = _catchesAir()
  def catchesGround             : Boolean   = _catchesGround()
  def splashesAir               : Boolean   = _splashesAir()
  def splashesGround            : Boolean   = _splashesGround()
  def hasGround                 : Boolean   = _hasGround()
  def hasAir                    : Boolean   = _hasAir()
  def engagingOn                : Boolean   = _engagingOn()
  def engagedUpon               : Boolean   = _engagedUpon()
  def volleyConsensus           : Boolean   = _volleyConsensus()
  def minersThreatened          : Boolean   = _minersThreatened()
  def centroidKey               : Pixel     = _centroidKey()
  def centroidAir               : Pixel     = _centroidAir()
  def centroidGround            : Pixel     = _centroidGround()
  def attackCentroidAir         : Pixel     = _attackCentroidAir()
  def attackCentroidGround      : Pixel     = _attackCentroidGround()
  def attackCentroidKey         : Pixel     = _attackCentroidKey()
  def destinationNext           : Pixel     = _destinationNext()
  def destinationFinal          : Pixel     = _destinationFinal()
  def destinationFinalDistance  : Double    = _destinationFinalDistance()
  def widthPixels               : Double    = _widthPixels()
  def meanTopSpeed              : Double    = _meanTopSpeed()
  def meanAttackerSpeed         : Double    = _meanAttackerSpeed()
  def meanAttackerRange         : Double    = _meanAttackerRange()
  def meanAttackerTargetDistance: Double    = _meanAttackerTargetDistance()
  def meanAttackerThreatDistance: Double    = _meanAttackerThreatDistance()
  def meanAttackerHealth        : Double    = _meanAttackerHealth()
  def meanArmor                 : Double    = _meanArmor()
  def meanArmorAir              : Double    = _meanArmorAir()
  def meanArmorGround           : Double    = _meanArmorGround()
  def meanDpf                   : Double    = _meanDpf()
  def meanDamageOnHit           : Double    = _meanDamageOnHit()
  def meanDistanceTarget        : Double    = _meanDistanceTarget()
  def maxAttackerSpeedVsGround  : Double    = _maxAttackerSpeedVsGround()
  def maxAttackerSpeedVsAir     : Double    = _maxAttackerSpeedVsAir()
  def maxRangeGround            : Double    = _maxRangeGround()
  def engagingOn01              : Double    = _engagingOn01()
  def engagedUpon01             : Double    = _engagedUpon01()
  def pace01                    : Double    = _pace01()
  def combatGroundFraction      : Double    = _combatGroundFraction()
  def consensusPrimaryFoes      : UnitGroup = _consensusPrimaryFoes()
  def keyDistanceTo       (pixel: Pixel)  : Double  = ?(hasGround, centroidKey       .groundPixels(pixel.walkablePixel), centroidKey       .pixelDistance(pixel))
  def attackKeyDistanceTo (pixel: Pixel)  : Double  = ?(hasGround, attackCentroidKey .groundPixels(pixel.walkablePixel), attackCentroidKey .pixelDistance(pixel))
  def canAttackIfVisible  (unit: UnitInfo): Boolean = ?(unit.flying, attacksAir, attacksGround)
  def canAttack           (unit: UnitInfo): Boolean = canAttackIfVisible(unit) && ( ! unit.cloaked || mobileDetectors.nonEmpty || With.units.existsOurs(Terran.Comsat))
  def canBeAttackedBy     (unit: UnitInfo): Boolean = (unit.canAttackGround && hasGround) || (unit.canAttackAir && hasAir)

  private val _count = new mutable.HashMap[UnitFilter, Int]()
  def count(matcher: UnitFilter): Int = _count.getOrElseUpdate(matcher, groupUnits.count(matcher))
  def has(matchers: UnitFilter*): Boolean = matchers.exists(count(_) > 0)

  private val _paceAge = 24
  private def _mobileUnits()              = groupOrderable.view.filter(_.canMove)
  private def _attackers()                = groupOrderable.view.filter(isAttacker)
  private def _attackersNonWorker()       = attackers.filterNot(_.unitClass.isWorker)
  private val _detectors                  = new Cache(() => groupOrderable.filter(u => u.aliveAndComplete && u.unitClass.isDetector).toVector)
  private val _mobileDetectors            = new Cache(() => detectors.filter(_.canMove))
  private val _arbiters                   = new Cache(() => groupOrderable.filter(u => u.aliveAndComplete && Protoss.Arbiter(u)).toVector)
  private val _repairables                = new Cache(() => groupOrderable.filter(_.unitClass.repairThreshold > 0))
  private val _repairableVIPs             = new Cache(() => repairables.filter(_.isAny(Terran.Battlecruiser, IsTank, Terran.Valkyrie)))
  private val _battles                    = new Cache(() => groupUnits.flatMap(_.battle).toSet)
  private val _battleEnemies              = new Cache(() => battles.flatMap(_.enemy.units))
  private val _roadblocks                 = new Cache(() => battleEnemies.filter(e => Math.max(keyDistanceTo(e.pixel), e.pixelDistanceTravelling(destinationFinal)) < destinationFinalDistance))
  private val _warriorsCastersCount       = new Cache(() => warriorsCasters.size)
  private val _attackersBioCount          = new Cache(() => attackersBio.size)
  private val _stormCount                 = new Cache(() => groupOrderable.view.filter(Protoss.HighTemplar).filter(_.player.hasTech(Protoss.PsionicStorm)).map(_.energy / 75).sum)
  private val _attacksAir                 = new Cache(() => attackers.exists(_.canAttackAir))
  private val _attacksGround              = new Cache(() => attackers.exists(_.canAttackGround))
  private val _catchesAir                 = new Cache(() => attackers.exists(a => (a.pixelRangeAir    > 96 || a.flying)  && a.canAttackAir))
  private val _catchesGround              = new Cache(() => attackers.exists(a => (a.pixelRangeGround > 96 || a.flying)  && a.canAttackGround))
  private val _splashesAir                = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackAir))
  private val _splashesGround             = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackGround))
  private val _hasGround                  = new Cache(() => groupOrderable.exists( ! _.flying))
  private val _hasAir                     = new Cache(() => groupOrderable.exists(   _.flying))
  private val _engagingOn                 = new Cache(() => groupOrderable.exists(_.matchups.engagingOn))
  private val _engagedUpon                = new Cache(() => groupOrderable.exists(_.matchups.engagedUpon))
  private val _volleyConsensus            = new Cache(() => Maff.modeOpt(attackers.flatMap(_.matchups.wantsToVolley)).getOrElse(false))
  private val _minersThreatened           = new Cache(() => consensusPrimaryFoes.attackers.exists(_.matchups.threateningMiners))
  private val _widthPixels                = new Cache(() => warriorsCasters.view.filterNot(_.flying).filter(_.canMove).map(_.unitClass.radialHypotenuse * 2).sum)
  private val _centroidKey                = new Cache(() => ?(_hasGround(), centroidGround, centroidAir))
  private val _centroidAir                = new Cache(() => GroupCentroid.air   (centroidUnits(Maff.orElse(           groupOrderable, groupUnits)), _.pixel))
  private val _centroidGround             = new Cache(() => GroupCentroid.ground(centroidUnits(Maff.orElse(           groupOrderable, groupUnits)), _.pixel))
  private val _attackCentroidAir          = new Cache(() => GroupCentroid.air   (centroidUnits(Maff.orElse(attackers, groupOrderable, groupUnits)), _.pixel))
  private val _attackCentroidGround       = new Cache(() => GroupCentroid.ground(centroidUnits(Maff.orElse(attackers, groupOrderable, groupUnits)), _.pixel))
  private val _attackCentroidKey          = new Cache(() => ?(_hasGround(), attackCentroidGround, attackCentroidAir))
  private val _destinationNext            = new Cache(() => GroupCentroid.ground(centroidUnits(attackers), _.presumptiveDestinationNext))
  private val _destinationFinal           = new Cache(() => GroupCentroid.ground(centroidUnits(attackers), _.presumptiveDestinationFinal))
  private val _destinationFinalDistance   = new Cache(() => keyDistanceTo(destinationFinal))
  private val _meanTopSpeed               = new Cache(() => Maff.mean(groupOrderable.view.filter(_.canMove).map(_.topSpeed)))
  private val _meanAttackerSpeed          = new Cache(() => Maff.mean(attackers.view.filter(_.canMove).map(_.topSpeed)))
  private val _meanAttackerRange          = new Cache(() => Maff.mean(attackers.view.map(_.pixelRangeMax)))
  private val _meanAttackerTargetDistance = new Cache(() => Maff.mean(attackers.flatMap(_.matchups.pixelsToTargetRange)))
  private val _meanAttackerThreatDistance = new Cache(() => Maff.mean(attackers.flatMap(_.matchups.pixelsToThreatRange)))
  private val _meanAttackerHealth         = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(_.totalHealth.toDouble)))
  private val _meanArmor                  = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(u => Maff.nanToZero(u.shieldPoints * u.armorShield + u.hitPoints * u.armorHealth) / u.totalHealth.toDouble)))
  private val _meanArmorAir               = new Cache(() => Maff.mean(Maff.orElse(attackers.filter(_.flying),     attackers, groupOrderable).view.map(u => Maff.nanToZero(u.shieldPoints * u.armorShield + u.hitPoints * u.armorHealth) / u.totalHealth.toDouble)))
  private val _meanArmorGround            = new Cache(() => Maff.mean(Maff.orElse(attackers.filterNot(_.flying),  attackers, groupOrderable).view.map(u => Maff.nanToZero(u.shieldPoints * u.armorShield + u.hitPoints * u.armorHealth) / u.totalHealth.toDouble)))
  private val _meanDpf                    = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(u => Math.max(u.dpfGround, u.dpfAir))))
  private val _meanDamageOnHit            = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(u => Math.max(u.damageOnHitGround, u.damageOnHitAir).toDouble)))
  private val _meanDistanceTarget         = new Cache(() => Maff.mean(attackers.filter(_.matchups.targetNearest.nonEmpty).map(u => u.matchups.targetNearest.get.pixelDistanceEdge(u))))
  private val _maxAttackerSpeedVsGround   = new Cache(() => Maff.max(attackers.filter(_.canAttackGround).map(_.topSpeed)).getOrElse(0.0))
  private val _maxAttackerSpeedVsAir      = new Cache(() => Maff.max(attackers.filter(_.canAttackAir).map(_.topSpeed)).getOrElse(0.0))
  private val _maxRangeGround             = new Cache(() => Maff.max(attackers.view.filter(_.canAttackGround).map(_.pixelRangeGround)).getOrElse(0.0))
  private val _engagingOn01               = new Cache(() => Maff.mean(attackers.view.map(a => Maff.toInt(a.matchups.engagingOn).toDouble)))
  private val _engagedUpon01              = new Cache(() => Maff.mean(attackers.view.map(a => Maff.toInt(a.matchups.engagedUpon).toDouble)))
  private val _pace01                     = new Cache(() => Maff.clamp(groupOrderable.view.map(u => (u.pixel - u.previousPixel(_paceAge)) / Math.max(0.01, u.topSpeed)).foldLeft(Pixel(0, 0))(_ + _).length / _paceAge / groupOrderable.length, -1, 1))
  private val _combatValueAir             = new Cache(() => warriorsCasters.filter(_.flying).map(_.subjectiveValue).sum)
  private val _combatValueGround          = new Cache(() => warriorsCasters.filterNot(_.flying).map(_.subjectiveValue).sum)
  private val _combatGroundFraction       = new Cache(() => Maff.nanToZero(_combatValueGround() / (_combatValueGround() + _combatValueAir())))
  private val _airToAirStrength           = new Cache(() => attackers.filterNot(Protoss.Interceptor).filterNot(Zerg.Guardian).map(u => u.friendly.filter(Protoss.Carrier).map(_.interceptorCount).getOrElse(u.unitClass.supplyRequired)).sum)
  private val _consensusPrimaryFoes       = new Cache(() => Maff.modeOpt(groupUnits.map(u => u.team.map(_.opponent).filter(_.warriorsCastersCount * 4 >= With.units.groupVs(u).warriorsCastersCount).getOrElse(With.units.groupVs(u)))).getOrElse(?(isInstanceOf[TFriendlyUnitGroup], With.units.enemyGroup, With.units.ourGroup)))

  protected def isAttacker(unit: UnitInfo): Boolean = unit.unitClass.canAttack && ! unit.unitClass.isWorker
  protected def centroidUnits(units: Iterable[UnitInfo]): Iterable[UnitInfo] = Maff.orElse(
    units.view.filter(_.likelyStillThere),
    units.view,
    groupOrderable.view.filter(_.likelyStillThere),
    groupOrderable.view.filter(_.likelyStillThere),
    groupUnits)

  override def toString: String = f"Group: $EnumerateUnits(groupUnits)"
}

package Tactic.Squads

import Information.Battles.Types.GroupCentroid
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?
import Utilities.UnitFilters.UnitFilter

import scala.collection.mutable

trait UnitGroup {
  def groupUnits        : Seq[UnitInfo]
  def groupOrderable    : Seq[UnitInfo] = groupUnits.view.filter(_.unitClass.orderable)
  def attackers         : Seq[UnitInfo] = _attackers()
  def detectors         : Seq[UnitInfo] = _detectors().view
  def mobileDetectors   : Seq[UnitInfo] = _mobileDetectors()
  def arbiters          : Seq[UnitInfo] = _arbiters().view
  def attackersCasters  : Seq[UnitInfo] = groupOrderable.view.filter(_.unitClass.attacksOrCasts)
  def attackersBio      : Seq[UnitInfo] = groupOrderable.view.filter(_.isAny(Terran.Marine, Terran.Firebat))
  def attackersCloaky   : Seq[UnitInfo] = groupOrderable.view.filter(u => u.isAny(Terran.Wraith, Terran.Ghost, Protoss.Arbiter, Protoss.DarkTemplar, Zerg.Lurker))
  def attackersCastersCount     : Int       = _attackersCastersCount()
  def attackersBioCount         : Int       = _attackersBioCount()
  def stormCount                : Int       = _stormCount()
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
  def centroidAir               : Pixel     = _centroidAir()
  def centroidGround            : Pixel     = _centroidGround()
  def centroidKey               : Pixel     = _centroidKey()
  def attackCentroidAir         : Pixel     = _attackCentroidAir()
  def attackCentroidGround      : Pixel     = _attackCentroidGround()
  def attackCentroidKey         : Pixel     = _attackCentroidKey()
  def widthPixels               : Double    = _widthPixels()
  def meanTopSpeed              : Double    = _meanTopSpeed()
  def meanAttackerSpeed         : Double    = _meanAttackerSpeed()
  def meanAttackerRange         : Double    = _meanAttackerRange()
  def meanAttackerTargetDistance: Double    = _meanAttackerTargetDistance()
  def meanAttackerHealth        : Double    = _meanAttackerHealth()
  def meanDpf                   : Double    = _meanDpf()
  def meanDistanceTarget        : Double    = _meanDistanceTarget()
  def maxAttackerSpeedVsGround  : Double    = _maxAttackerSpeedVsGround()
  def maxAttackerSpeedVsAir     : Double    = _maxAttackerSpeedVsAir()
  def maxRangeGround            : Double    = _maxRangeGround()
  def engagingOn01              : Double    = _engagingOn01()
  def engagedUpon01             : Double    = _engagedUpon01()
  def pace01                    : Double    = _pace01()
  def combatGroundFraction      : Double    = _combatGroundFraction()
  def consensusPrimaryFoes      : UnitGroup = _consensusPrimaryFoes()
  def keyDistanceTo       (pixel: Pixel): Double = if (hasGround) centroidKey       .groundPixels(pixel.walkablePixel) else centroidKey       .pixelDistance(pixel)
  def attackKeyDistanceTo (pixel: Pixel): Double = if (hasGround) attackCentroidKey .groundPixels(pixel.walkablePixel) else attackCentroidKey .pixelDistance(pixel)
  def canAttack(unit: UnitInfo): Boolean = if (unit.flying) attacksAir else attacksGround
  def canBeAttackedBy(unit: UnitInfo): Boolean = unit.canAttackGround && hasGround || unit.canAttackAir && hasAir

  private val _count = new mutable.HashMap[UnitFilter, Int]()
  def count(matcher: UnitFilter): Int = {
    _count(matcher) = _count.getOrElse(matcher, groupUnits.count(matcher))
    _count(matcher)
  }

  private val _paceAge = 24
  private def _attackers()                = groupOrderable.view.filter(isAttacker)
  private val _detectors                  = new Cache(() => groupOrderable.filter(u => u.aliveAndComplete && u.unitClass.isDetector).toVector)
  private val _mobileDetectors            = new Cache(() => detectors.filter(_.canMove))
  private val _arbiters                   = new Cache(() => groupOrderable.filter(u => u.aliveAndComplete && Protoss.Arbiter(u)).toVector)
  private val _attackersCastersCount      = new Cache(() => attackersCasters.size)
  private val _attackersBioCount          = new Cache(() => attackersBio.size)
  private val _stormCount                 = new Cache(() => groupOrderable.view.filter(Protoss.HighTemplar).filter(_.player.hasTech(Protoss.PsionicStorm)).map(_.energy / 75).sum)
  private val _attacksAir                 = new Cache(() => attackers.exists(_.canAttackAir))
  private val _attacksGround              = new Cache(() => attackers.exists(_.canAttackGround))
  private val _catchesAir                 = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  private val _catchesGround              = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))
  private val _splashesAir                = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackAir))
  private val _splashesGround             = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackGround))
  private val _hasGround                  = new Cache(() => groupOrderable.exists( ! _.flying))
  private val _hasAir                     = new Cache(() => groupOrderable.exists(_.flying))
  private val _engagingOn                 = new Cache(() => groupOrderable.exists(_.matchups.engagingOn))
  private val _engagedUpon                = new Cache(() => groupOrderable.exists(_.matchups.engagedUpon))
  private val _volleyConsensus            = new Cache(() => Maff.modeOpt(attackers.flatMap(_.matchups.wantsToVolley)).getOrElse(false))
  private val _widthPixels                = new Cache(() => attackersCasters.view.filterNot(_.flying).filter(_.canMove).map(_.unitClass.radialHypotenuse * 2).sum)
  private val _centroidAir                = new Cache(() => GroupCentroid.air(centroidUnits(groupOrderable)))
  private val _centroidGround             = new Cache(() => GroupCentroid.ground(centroidUnits(groupOrderable)))
  private val _centroidKey                = new Cache(() => if (_hasGround()) centroidGround else centroidAir)
  private val _attackCentroidAir          = new Cache(() => GroupCentroid.air(centroidUnits(attackers)))
  private val _attackCentroidGround       = new Cache(() => GroupCentroid.ground(centroidUnits(attackers)))
  private val _attackCentroidKey          = new Cache(() => if (_hasGround()) attackCentroidGround else attackCentroidAir)
  private val _meanTopSpeed               = new Cache(() => Maff.mean(groupOrderable.view.filter(_.canMove).map(_.topSpeed)))
  private val _meanAttackerSpeed          = new Cache(() => Maff.mean(attackers.view.filter(_.canMove).map(_.topSpeed)))
  private val _meanAttackerRange          = new Cache(() => Maff.mean(attackers.view.map(_.pixelRangeMax)))
  private val _meanAttackerTargetDistance = new Cache(() => Maff.mean(attackers.flatMap(_.matchups.pixelsToTargetRange)))
  private val _meanAttackerHealth         = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(_.totalHealth.toDouble)))
  private val _meanDpf                    = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(u => Math.max(u.dpfGround, u.dpfAir))))
  private val _meanDistanceTarget         = new Cache(() => Maff.mean(attackers.filter(_.matchups.targetNearest.nonEmpty).map(u => u.matchups.targetNearest.get.pixelDistanceEdge(u))))
  private val _maxAttackerSpeedVsGround   = new Cache(() => Maff.max(attackers.filter(_.canAttackGround).map(_.topSpeed)).getOrElse(0.0))
  private val _maxAttackerSpeedVsAir      = new Cache(() => Maff.max(attackers.filter(_.canAttackAir).map(_.topSpeed)).getOrElse(0.0))
  private val _maxRangeGround             = new Cache(() => Maff.max(attackers.view.filter(_.canAttackGround).map(_.pixelRangeGround)).getOrElse(0.0))
  private val _engagingOn01               = new Cache(() => Maff.mean(attackers.view.map(a => Maff.toInt(a.matchups.engagingOn).toDouble)))
  private val _engagedUpon01              = new Cache(() => Maff.mean(attackers.view.map(a => Maff.toInt(a.matchups.engagedUpon).toDouble)))
  private val _pace01                     = new Cache(() => Maff.clamp(groupOrderable.view.map(u => (u.pixel - u.previousPixel(_paceAge)) / Math.max(0.01, u.topSpeed)).foldLeft(Pixel(0, 0))(_ + _).length / _paceAge / groupOrderable.length, -1, 1))
  private val _combatValueAir             = new Cache(() => attackersCasters.filter(_.flying).map(_.subjectiveValue).sum)
  private val _combatValueGround          = new Cache(() => attackersCasters.filterNot(_.flying).map(_.subjectiveValue).sum)
  private val _combatGroundFraction       = new Cache(() => Maff.nanToZero(_combatValueGround() / (_combatValueGround() + _combatValueAir())))
  private val _consensusPrimaryFoes       = new Cache(() => Maff.modeOpt(groupUnits.map(u => u.team.map(_.opponent).filter(_.attackersCastersCount * 4 >= With.units.groupVs(u).attackersCastersCount).getOrElse(With.units.groupVs(u)))).getOrElse(?(isInstanceOf[TFriendlyUnitGroup], With.units.enemyGroup, With.units.ourGroup)))

  protected def isAttacker(unit: UnitInfo): Boolean = unit.unitClass.canAttack && ! unit.unitClass.isWorker
  protected def centroidUnits(units: Iterable[UnitInfo]): Iterable[UnitInfo] = Maff.orElse(units.view.filter(_.likelyStillThere), units.view)
}

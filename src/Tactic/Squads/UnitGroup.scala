package Tactic.Squads

import Information.Battles.Types.GroupCentroid
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.UnitFilters.UnitFilter

import scala.collection.mutable

trait UnitGroup {
  def groupUnits        : Seq[UnitInfo]
  def groupOrderable    : Seq[UnitInfo] = groupUnits.view.filter(_.unitClass.orderable)
  def attackers         : Seq[UnitInfo] = _attackers()
  def detectors         : Seq[UnitInfo] = _detectors()
  def attackersCasters  : Seq[UnitInfo] = groupOrderable.view.filter(_.unitClass.attacksOrCasts)
  def attackersBio      : Seq[UnitInfo] = groupOrderable.view.filter(_.isAny(Terran.Marine, Terran.Firebat))
  def attackersCastersCount             = _attackersCastersCount()
  def attackersBioCount                 = _attackersBioCount()
  def attacksAir                        = _attacksAir()
  def attacksGround                     = _attacksGround()
  def catchesAir                        = _catchesAir()
  def catchesGround                     = _catchesGround()
  def splashesAir                       = _splashesAir()
  def splashesGround                    =  _splashesGround()
  def hasGround                         = _hasGround()
  def engagingOn                        = _engagingOn()
  def engagedUpon                       = _engagedUpon()
  def widthPixels                       = _widthPixels()
  def centroidAir                       = _centroidAir()
  def centroidGround                    = _centroidGround()
  def centroidKey                       = _centroidKey()
  def attackCentroidAir                 = _centroidAir()
  def attackCentroidGround              = _centroidGround()
  def attackCentroidKey                 = _centroidKey()
  def meanTopSpeed                      = _meanTopSpeed()
  def meanAttackerSpeed                 = _meanAttackerSpeed()
  def meanAttackerRange                 = _meanAttackerRange()
  def meanTotalHealth                   = _meanTotalHealth()
  def meanDpf                           = _meanDpf()
  def storms                            = _storms()
  def pace01                            = _pace01()
  def combatGroundFraction              = _combatGroundFraction()
  def keyDistanceTo(pixel: Pixel): Double = if (hasGround) centroidKey.groundPixels(pixel.walkablePixel) else centroidKey.pixelDistance(pixel)
  def attackKeyDistanceTo(pixel: Pixel): Double = if (hasGround) attackCentroidKey.groundPixels(pixel.walkablePixel) else attackCentroidKey.pixelDistance(pixel)

  private val _count = new mutable.HashMap[UnitFilter, Int]()
  def count(matcher: UnitFilter): Int = {
    _count(matcher) = _count.getOrElse(matcher, groupUnits.count(matcher))
    _count(matcher)
  }

  private val _paceAge = 24
  private def _attackers()              = groupOrderable.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker)
  private def _detectors()              = groupOrderable.view.filter(u => u.aliveAndComplete && u.unitClass.isDetector)
  private val _attackersCastersCount    = new Cache(() => attackersCasters.size)
  private val _attackersBioCount        = new Cache(() => attackersBio.size)
  private val _attacksAir               = new Cache(() => attackers.exists(_.canAttackAir))
  private val _attacksGround            = new Cache(() => attackers.exists(_.canAttackGround))
  private val _catchesAir               = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  private val _catchesGround            = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))
  private val _splashesAir              = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackAir))
  private val _splashesGround           = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackGround))
  private val _hasGround                = new Cache(() => groupOrderable.exists( ! _.flying))
  private val _engagingOn               = new Cache(() => groupOrderable.exists(u =>                         u.matchups.targetsInRange.nonEmpty))
  private val _engagedUpon              = new Cache(() => groupOrderable.exists(u => u.visibleToOpponents && u.matchups.threatsInRange.nonEmpty))
  private val _widthPixels              = new Cache(() => attackersCasters.view.filterNot(_.flying).filter(_.canMove).map(_.unitClass.radialHypotenuse * 2).sum)
  private val _centroidAir              = new Cache(() => GroupCentroid.air(centroidUnits(groupOrderable)))
  private val _centroidGround           = new Cache(() => GroupCentroid.ground(centroidUnits(groupOrderable)))
  private val _centroidKey              = new Cache(() => if (_hasGround()) centroidGround else centroidAir)
  private val _attackCentroidAir        = new Cache(() => GroupCentroid.air(centroidUnits(attackers)))
  private val _attackCentroidGround     = new Cache(() => GroupCentroid.ground(centroidUnits(attackers)))
  private val _attackCentroidKey        = new Cache(() => if (_hasGround()) attackCentroidGround else attackCentroidAir)
  private val _meanTopSpeed             = new Cache(() => Maff.mean(groupOrderable.view.filter(_.canMove).map(_.topSpeed)))
  private val _meanAttackerSpeed        = new Cache(() => Maff.mean(attackers.view.filter(_.canMove).map(_.topSpeed)))
  private val _meanAttackerRange        = new Cache(() => Maff.mean(attackers.view.map(_.pixelRangeMax)))
  private val _meanTotalHealth          = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(_.hitPoints.toDouble)))
  private val _meanDpf                  = new Cache(() => Maff.mean(Maff.orElse(attackers, groupOrderable).view.map(u => Math.max(u.dpfGround, u.dpfAir))))
  private val _storms                   = new Cache(() => groupOrderable.view.filter(Protoss.HighTemplar).filter(_.player.hasTech(Protoss.PsionicStorm)).map(_.energy / 75).sum)
  private val _pace01                   = new Cache(() => Maff.clamp(groupOrderable.view.map(u => (u.pixel - u.previousPixel(_paceAge)) / Math.max(0.01, u.topSpeed)).foldLeft(Pixel(0, 0))(_ + _).length / _paceAge, -1, 1))
  private val _combatValueAir           = new Cache(() => attackersCasters.filter(_.flying).map(_.subjectiveValue).sum)
  private val _combatValueGround        = new Cache(() => attackersCasters.filterNot(_.flying).map(_.subjectiveValue).sum)
  private val _combatGroundFraction     = new Cache(() => Maff.nanToZero(_combatValueGround() / (_combatValueGround() + _combatValueAir())))
  private def centroidUnits(units: Iterable[UnitInfo]): Iterable[UnitInfo] = Maff.orElse(units.view.filter(_.likelyStillThere), units.view)

}

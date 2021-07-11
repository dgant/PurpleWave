package Tactics.Squads

import Information.Battles.Types.GroupCentroid
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.SeqView

trait UnitGroup {
  def groupUnits: Seq[UnitInfo]

  def groupOrderable: SeqView[UnitInfo, Seq[UnitInfo]] = groupUnits.view.filter(_.unitClass.orderable)
  def attackers       = _attackers()
  def detectors       = _detectors()
  def attacksAir      = _attacksAir()
  def attacksGround   = _attacksGround()
  def catchesAir      = _catchesAir()
  def catchesGround   = _catchesGround()
  def splashesAir     = _splashesAir()
  def splashesGround  = _splashesGround()
  def hasGround       = _hasGround()
  def engagingOn      = _engagingOn()
  def engagedUpon     = _engagedUpon()
  def centroidAll     = _centroidAll()
  def centroidAir     = _centroidAir()
  def centroidGround  = _centroidGround()
  def centroidKey     = _centroidKey()
  def battleConsensus = _battleConsensus()
  def threatConsensus = _threatConsensus()

  private def _attackers()      = groupOrderable.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker)
  private def _detectors()      = groupOrderable.view.filter(u => u.aliveAndComplete && u.unitClass.isDetector)
  private val _attacksAir       = new Cache(() => attackers.exists(_.canAttackAir))
  private val _attacksGround    = new Cache(() => attackers.exists(_.canAttackGround))
  private val _catchesAir       = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  private val _catchesGround    = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))
  private val _splashesAir      = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackAir))
  private val _splashesGround   = new Cache(() => attackers.exists(a => a.unitClass.dealsRadialSplashDamage && a.canAttackGround))
  private val _hasGround        = new Cache(() => groupOrderable.exists( ! _.flying))
  private val _engagingOn       = new Cache(() => groupOrderable.exists(_.matchups.targetsInRange.nonEmpty))
  private val _engagedUpon      = new Cache(() => groupOrderable.exists(_.matchups.pixelsOfEntanglement >= 0))
  private val _centroidAll      = new Cache(() => Maff.centroid(groupOrderable.view.map(_.pixel)))
  private val _centroidAir      = new Cache(() => GroupCentroid.air(groupOrderable))
  private val _centroidGround   = new Cache(() => GroupCentroid.ground(groupOrderable))
  private val _centroidKey      = new Cache(() => if (_hasGround()) centroidGround else centroidAir )
  private val _battleConsensus  = new Cache(() => Maff.mode(groupOrderable.view.map(_.battle)))
  private val _threatConsensus  = new Cache(() => battleConsensus.map(_.enemy.centroidGround))
}
